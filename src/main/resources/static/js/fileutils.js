/**
 * @Date: 2024.05.30
 * 1. jpg 파일을 읽어서 exif 데이터를 확인한다.
 * 2. exif 데이터를 읽어서 회전정보를 확인하고 회전정보에 따라 이미지를 회전시킨다.
 * 3. 회전된 이미지 크기를 줄인다.
 * 4. 이미지의 메타정보에서 필요한 정보를 추가한다.
 * 5. 이미지를 서버로 전송한다.
 * 이미지는 s3와 동일한 사양인 minio를 사용한다. (https://min.io/), 브라우저에서 직접 저장하지 않고 서버에서 저장하게 한다.
 * 이유는, 메타정보를 읽어서 DB처리, 이미지를 저장하는 것이 더 효율적이기 때문이다.
 * uuid는 이미지를 저장할 gpx파일의 메인키이다. gpx파일을 저장하고 이미지를 저장할 때 gpx파일의 uuid를 사용한다.
 * @param file(MultiPartFile)
 */
function processFile(uuid, file) {
    return new Promise((resolve, reject) => {
        if (uuid == '' || file == null) {
            alert('파일이 저장되지 않았거나 파일이 없습니다..');
            return;
        }
        let savedFilename = '';
        let reader = new FileReader();
        let savedFileInfo;

        reader.onload = function (e) {
            let arrayBuffer = e.target.result;
            let exifData = ExifReader.load(arrayBuffer);
            console.log(exifData);

            let orientation = exifData.Orientation ? exifData.Orientation.value : 1;

            let image = new Image();
            image.src = URL.createObjectURL(file);

            image.onload = function () {
                // 이미지 압축
                let canvas = document.createElement('canvas');
                let ctx = canvas.getContext('2d');

                let maxWidth = 1024;
                let maxHeight = 1024;
                let width = image.width;
                let height = image.height;

                if (width > height) {
                    if (width > maxWidth) {
                        height *= maxWidth / width;
                        width = maxWidth;
                    }
                } else {
                    if (height > maxHeight) {
                        width *= maxHeight / height;
                        height = maxHeight;
                    }
                }

                canvas.width = width;
                canvas.height = height;

                switch (orientation) {
                    case 2:
                        ctx.transform(-1, 0, 0, 1, canvas.width, 0);
                        break;
                    case 3:
                        ctx.transform(-1, 0, 0, -1, canvas.width, canvas.height);
                        break;
                    case 4:
                        ctx.transform(1, 0, 0, -1, 0, canvas.height);
                        break;
                    case 5:
                        ctx.transform(0, 1, 1, 0, 0, 0);
                        break;
                    case 6:
                        ctx.transform(0, 1, -1, 0, canvas.height, 0);
                        break;
                    case 7:
                        ctx.transform(0, -1, -1, 0, canvas.height, canvas.width);
                        break;
                    case 8:
                        ctx.transform(0, -1, 1, 0, 0, canvas.width);
                        break;
                    default:
                        ctx.transform(1, 0, 0, 1, 0, 0);
                }

                ctx.drawImage(image, 0, 0, width, height);

                // 압축된 이미지 Blob 생성
                canvas.toBlob(function (blob) {
                    // EXIF 데이터 유지
                    let reader = new FileReader();
                    reader.onloadend = function () {
                        let base64Data = reader.result.split(',')[1];
                        let newDataUrl = 'data:image/jpeg;base64,' + base64Data;

                        let jpeg = {};
                        let exif = {};
                        let gps = {};
                        let thumbnail = {};

                        jpeg[piexif.ImageIFD.Make] = exifData["Make"].value;
                        jpeg[piexif.ImageIFD.Model] = exifData["Model"].value;
                        jpeg[piexif.ImageIFD.Orientation] = exifData["Orientation"].value;
                        jpeg[piexif.ImageIFD.ImageWidth] = [width, 1];
                        jpeg[piexif.ImageIFD.ImageLength] = [height, 1];
                        jpeg[piexif.ImageIFD.DateTime] = exifData["DateTime"].value;

                        exif[piexif.ExifIFD.ExifVersion] = exifData["ExifVersion"].value;
                        //exif[piexif.ExifIFD.DateTimeOriginal]  = exifData["DateTimeOriginal"].value;

                        gps[piexif.GPSIFD.GPSLatitudeRef] = exifData["GPSLatitudeRef"].value;
                        gps[piexif.GPSIFD.GPSLatitude] = exifData["GPSLatitude"].value;
                        gps[piexif.GPSIFD.GPSLongitudeRef] = exifData["GPSLongitudeRef"].value;
                        gps[piexif.GPSIFD.GPSLongitude] = exifData["GPSLongitude"].value;
                        gps[piexif.GPSIFD.GPSAltitudeRef] = exifData["GPSAltitudeRef"].value;
                        gps[piexif.GPSIFD.GPSAltitude] = exifData["GPSAltitude"].value;

                        let exifObj = {"0th": jpeg, "Exif": exif, "GPS": gps}; //이해가 안되네...
                        console.log(exifObj);

                        let exifStr = piexif.dump(exifObj);

                        let newDataUrlWithExif = piexif.insert(exifStr, newDataUrl);
                        let newBlob = dataURLtoBlob(newDataUrlWithExif);

                        // 서버로 POST 요청 전송
                        let formData = new FormData();
                        formData.append('file', newBlob, file.name);
                        formData.append('uuid', uuid);

                        $.ajax({
                            url: '/api/1.0/imageUpload',
                            type: 'POST',
                            data: formData,
                            contentType: false,
                            processData: false,
                            success: function (response) {
                                savedFileInfo = response.data;
                                //drawUserMarkImage(response.data.filePath, response.data.geoLocation);
                                resolve(response.data);  // Promise 해결
                                console.log('File uploaded successfully.');
                            },
                            error: function (jqXHR, textStatus, errorThrown) {
                                alert('File upload failed.');
                                reject(errorThrown);  // Promise 거부
                            }
                        });
                    };
                    reader.readAsDataURL(blob);
                }, 'image/jpeg', 0.7); // 이미지 품질 설정
            }
        };
        reader.readAsArrayBuffer(file);
    });
        //return savedFileInfo;
}

function dataURLtoBlob(dataurl) {
    let arr = dataurl.split(','), mime = arr[0].match(/:(.*?);/)[1];
    let bstr = atob(arr[1]), n = bstr.length, u8arr = new Uint8Array(n);
    while (n--) {
        u8arr[n] = bstr.charCodeAt(n);
    }
    return new Blob([u8arr], { type: mime });
}
