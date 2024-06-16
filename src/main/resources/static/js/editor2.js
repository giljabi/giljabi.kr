let _drawingManager;
let _drawingManagerOption;
let _globalMap;
let _gpxTrkseqArray = new Array();		//gpx/trk/trkseq
let BASETIME = new Date();
let eleFalg = false;	//고도정보를 받아온 경우 true
let poiCategory = '';

// 배경으로 사용할 GPX 정보
let basePolyline = [];
let baseTrkList = [];
let baseWptList = [];
let _filetype = 'gpx';

function addListener() {
    kakao.maps.event.addListener(_globalMap, 'dragend', function () {
        //removeCategryMarker();
    });

    // 지도가 확대 또는 축소되면 마지막 파라미터로 넘어온 함수를 호출하도록 이벤트를 등록합니다
    kakao.maps.event.addListener(_globalMap, 'zoom_changed', function () {
        //searchPlaces();
    });
}

function initDrawing() {
    _drawingManagerOption = { // Drawing Manager를 생성할 때 사용할 옵션입니다
        map: _globalMap, // Drawing Manager로 그리기 요소를 그릴 map 객체입니다
        drawingMode: [ // drawing manager로 제공할 그리기 요소 모드입니다
            kakao.maps.drawing.OverlayType.MARKER,
            kakao.maps.drawing.OverlayType.POLYLINE
        ],
        // 사용자에게 제공할 그리기 가이드 툴팁입니다
        // 사용자에게 도형을 그릴때, 드래그할때, 수정할때 가이드 툴팁을 표시하도록 설정합니다
        guideTooltip: ['draw', 'drag', 'edit'],
        markerOptions: { // 마커 옵션입니다
            draggable: true, // 마커를 그리고 나서 드래그 가능하게 합니다
            removable: true // 마커를 삭제 할 수 있도록 x 버튼이 표시됩니다
        },
        polylineOptions: { // 선 옵션입니다
            draggable: true, // 그린 후 드래그가 가능하도록 설정합니다
            removable: true, // 그린 후 삭제 할 수 있도록 x 버튼이 표시됩니다
            editable: true, // 그린 후 수정할 수 있도록 설정합니다
            strokeColor: '#ff0000', // 선 색
            hintStrokeStyle: 'dash', // 그리중 마우스를 따라다니는 보조선의 선 스타일
            hintStrokeOpacity: 0.5  // 그리중 마우스를 따라다니는 보조선의 투명도
        }
    };
    //위에 작성한 옵션으로 Drawing Manager를 생성합니다
    _drawingManager = new kakao.maps.drawing.DrawingManager(_drawingManagerOption);
}

function initMap() {
    let options = {
        center: getLocation(), //Seoul city hall
        level: 8
    };
    _globalMap = new kakao.maps.Map(document.getElementById('map'), options);

    let mapTypeControl = new kakao.maps.MapTypeControl(); // 지도타입 컨트롤
    _globalMap.addControl(mapTypeControl, kakao.maps.ControlPosition.TOPRIGHT);

    // 지도 확대 축소를 제어할 수 있는  줌 컨트롤을 생성합니다
    let zoomControl = new kakao.maps.ZoomControl();
    _globalMap.addControl(zoomControl, kakao.maps.ControlPosition.RIGHT);

    combo100();         //100개 산 목록을 콤보박스에 채운다.

}

function onClickFileInput() {
    $('#fileInput').change(function () {
        let file = document.getElementById('fileInput').files[0];
        _uploadFilename = file.name.substring(0, file.name.lastIndexOf('.'));
        _fileExt = file.name.substring(file.name.lastIndexOf('.') + 1);
        let reader = new FileReader();

        reader.onload = function (e) {
            makeObject(reader.result);
            console.log(reader.result);	//필요하면 디버깅으로...
        };

        reader.readAsText(file);
    });
}

// combo box에 산 목록을 채운다
function combo100() {
    $.ajax({
        type: 'get',
        url: '/api/1.0/mountain100',
        contentType: 'application/json',
        dataType: 'json',
        async: false,
        complete: function () {
        },
        success: function (response, status) {
            if (response.status === 0) {
                response.data.forEach(function (mountain) {
                    $('#mountain100Select').append($('<option></option>').val(mountain.filename).html(mountain.name));
                });
                //console.log($('#mountain100Select').html());
            } else {
                console.log(response.message);
            }
        },
    });
}

function onClickAll100() {
    $('#all100').click(function () {
        //산목록
        let mountain100Select = $('#mountain100Select option').map(function () {
            return $(this).val();
        }).get();
        //첫째 데이터는 제외한다.
        mountain100Select.shift();
        let totalMountains = mountain100Select.length;
        let completedTasks = 0;

        mountain100Select.forEach(function (mountain, index) {
            // Calculate delay based on index
            setTimeout(function () {
                let mountainGpxList = getMountainGpxLists(mountain);
                getMountainGpx(mountainGpxList);
                completedTasks++;
                let percentComplete = (completedTasks / totalMountains) * 100;

                $('#progress-bar').val(Math.round(percentComplete) + '%');

                if (completedTasks === totalMountains) {
                    $('#progress-bar').val("");
                }
            }, 500 * index); // Delay increases by 1 second each iteration
        });
        $('#all100')
            .css({'color': '#aaa', 'opacity': '0.5'})
            .prop('disabled', true);
    });
}

function onSelectByCombo() {
    $('#mountain100Select').on('change', function () {
        // this를 사용하여 현재 선택된 옵션의 value를 얻음
        let selectedValue = $(this).val();
        let mountainList = getMountainGpxLists(selectedValue);
        //console.log(mountainList);
        getMountainGpx(mountainList);
    });
}

/**
 * 1개 산은 여러개의 gpx 파일로 구성되어 gpx 목록을 가져온다.
 * @param selectedValue
 * @returns {*[]}
 */
function getMountainGpxLists(mountainName) {
    let mountainList = [];
    $.ajax({
        type: 'get',
        url: '/api/1.0/mountainGpxLists/' + mountainName,
        contentType: 'application/json',
        dataType: 'json',
        async: false,
        complete: function () {
        },
        success: function (response, status) {
            if (response.status === 0) {
                mountainList = response.data;
            } else {
                console.log(response.message);
            }
        },
    });
    return mountainList;
}

/**
 * gpx 목록을 비동기식으로 받아서 화면에 그려준다.
 * @param mountainList
 */
function getMountainGpx(mountainList) {
    $.each(mountainList, function (index, ele) {
        $.ajax({
            type: 'get',
            url: '/api/1.0/mountainGpx/' + mountainList[index],
            contentType: 'application/json',
            dataType: 'json',
            async: false,
            complete: function () {
            },
            success: function (response, status) {
                if (response.status === 0) {
                    drawGpx(response);
                } else {
                    console.log(response.message);
                }
            },
        });
    });
}

function drawPlot() {
    if (_gpxTrkseqArray.length == 0) {
        alert('고도 정보가 없습니다.');
        return;
    }

    let legends = $("#elevationImage .legendLabel");
    let updateLegendTimeout = null;
    let latestPosition = null;
    let cursorMarker = new kakao.maps.Marker();

    let eleArray = [];		//경로의 높이정보
    let distance = 0;			//직전이동거리
    let odometer = Number(0);	//누적이동거리
    let minAlti = 0, maxAlti = 0;
    let curAlti = 0;
    for (let i = 1; i < _gpxTrkseqArray.length; i++) {
        distance = getDistance(_gpxTrkseqArray[i - 1], _gpxTrkseqArray[i]);
        odometer += distance / 1000;

        curAlti = _gpxTrkseqArray[i - 1].ele;	//고도

        if (curAlti >= maxAlti) maxAlti = curAlti; //전체 경로에서 최대높이
        if (curAlti <= minAlti) minAlti = curAlti; //전체 경로에서 최저높이

        //누적거리와 고도정보
        eleArray.push([odometer, _gpxTrkseqArray[i - 1].ele]);
    }
    eleArray.push([odometer, _gpxTrkseqArray[_gpxTrkseqArray.length - 1].ele]);

    //참고 http://www.flotcharts.org/flot/examples/tracking/index.html
    plot = $.plot("#elevationImage", [{data: eleArray}], {
        //series: { lines: { show: true }},
        crosshair: {mode: "x"},
        grid: {hoverable: true, autoHighlight: false, show: true, aboveData: true},
        yaxis: {min: minAlti * 0.7, max: maxAlti * 1.2} //위/아래에 약간의 여유
    });

    function updateLegend() {
        updateLegendTimeout = null;
        let pos = latestPosition;
        let i, j, dataset = plot.getData();
        for (i = 0; i < dataset.length; ++i) {
            var series = dataset[i];
            for (j = 0; j < series.data.length; ++j) {
                if (series.data[j][0] > pos.x) {
                    break;
                }
            }
            let position;
            //고도차트에서 마무스를 따라 움직이는 지도상의 마커
            cursorMarker.setMap(null);	//마커를 삭제하고
            if (j == series.data.length) {
                position = new kakao.maps.LatLng(_gpxTrkseqArray[_gpxTrkseqArray.length - 1].lat,
                    _gpxTrkseqArray[_gpxTrkseqArray.length - 1].lng);
            } else {
                position = new kakao.maps.LatLng(_gpxTrkseqArray[j].lat, _gpxTrkseqArray[j].lng);
            }
            cursorMarker = new kakao.maps.Marker({
                position: position
            });
            cursorMarker.setMap(_globalMap);
        }
    }

    //차트에서 마우스의 움직임이 있으면....
    $("#elevationImage").bind("plothover", function (event, pos, item) {
        latestPosition = pos;
        if (!updateLegendTimeout) {
            updateLegendTimeout = setTimeout(updateLegend, 50);
        }
    });
}

function pointsToPath(points) {
    return points.map(point => new kakao.maps.LatLng(point.y, point.x));
}

function onClickGetElevation() {
    $('#getElevation').click(function () {
        /*        $('#editinfo').block({
                    message: '<h4>Processing</h1>',
                    css: {border: '3px solid #a00', width: '600px'}
                });*/

        let data = _drawingManager.getData();
        let len = data[kakao.maps.drawing.OverlayType.POLYLINE].length;
        if (len == 0) {
            alert('경로정보가 없습니다."경로 그리기" 기능으로 경로를 그린 후 사용하세요.');
            return;
        }

        $('#blockingAds').show();
        //모든 polyline
        let trkseq = new Array();	//servlet에 요청하기 위한 배열 object를 string으로 변환
        for (let i = 0; i < len; i++) {
            let line = pointsToPath(data.polyline[i].points);
            for (let j = 0; j < line.length; j++) {
                trkseq.push({lat: line[j].getLat(), lng: line[j].getLng()});
            }
        }

        $.ajax({
            type: 'post',
            url: '/api/1.0/elevation',
            data: JSON.stringify({trackPoint: trkseq}),
            contentType: 'application/json',
            dataType: 'json',
            async: true,
            complete: function () {

            },
            success: function (response, status) {
                if (response.status === 0) {
                    _gpxTrkseqArray = [];
                    _gpxTrkseqArray = response.data;
                    eleFalg = true;
                    //$('#check').text(data.check);
                    drawPlot();
                } else {
                    eleFalg = false;
                    console.log(response.message);
                }
                $('#blockingAds').hide();
            },
        });
    });
}

function basePathLoadGpx(gpxfile, strokeColor) {
    let reader = $($.parseXML(gpxfile));
    $.each(reader.find('gpx').find('trk'), function () {
        let trkptList = [];
        $.each($(this).find('trkseg').find('trkpt'), function () {
            trkptList.push(new kakao.maps.LatLng(
                Number($(this).attr('lat')),
                Number($(this).attr('lon')))
            );
        });
        //baseTrkList.push(item); //삭제하기 위한 데이터, 삭제할 필요가 있을까??? 계속 추가해도 좋을듯

        let lineStyle = new kakao.maps.Polyline({
            map: _globalMap,
            path: trkptList,
            strokeColor: strokeColor, // 선의 색깔 '#A52A2A'
            strokeOpacity: 1, // 선의 불투명도, 1에서 0 사이의 값이며 0에 가까울수록 투명
            strokeStyle: 'solid', // 선의 스타일
            strokeWeight: 3
        });
        basePolyline.push(lineStyle);
        //moveCenterPoint(trkptList[parseInt(trkptList.length / 2)]);
    });

    //wpt 체크되어 있으면 웨이포인트를 표시한다.
    if ($('#wptIcon').prop('checked')) {
        $.each(reader.find('gpx').find('wpt'), function () {
            let item = new GpxWaypoint(
                Number($(this).attr('lat')),
                Number($(this).attr('lon')),
                Number($(this).find('ele')),
                $(this).find('name').text(),
                $(this).find('desc').text(),
                $(this).find('type').text(),
                getIconString($(this).find('sym').text())
            );
            //baseWptList.push(new WaypointMark(item.position, item.name, item.uid, item.sym));
        });
    }
}

function makeObject(xml) {
    let gpx = $($.parseXML(xml));
    loadGpx(gpx);

    //시작과 끝 표시
    makeMarkerPoint(_globalMap, 'start', _gpxTrkseqArray[0]);
    makeMarkerPoint(_globalMap, 'end', _gpxTrkseqArray[_gpxTrkseqArray.length - 1]);

    _drawingManager.put(kakao.maps.drawing.OverlayType.POLYLINE, _trkPoly);

}

function loadGpx(x) {
    $.each(x.find('gpx > trk > trkseg > trkpt'), function() {
        var trkpt = {
            lat: $(this).attr('lat'),
            lng: $(this).attr('lon'),
            ele: $(this).find('ele').text()
        };
        _gpxTrkseqArray.push(trkpt);
        _trkPoly.push(new kakao.maps.LatLng(trkpt.lat, trkpt.lng));
    });
}

//ajax로 gpx파일을 받아온 경우, 압축되어 있음
function drawGpx(response) {
    const decodedString = LZString.decompressFromUTF16(response.data.xmlData);
    basePathLoadGpx(decodedString, '#0037ff');
}

function onClickBaseInput() {
    $('#baseInput').change(function () {
        let file = document.getElementById('baseInput').files[0];
        let reader = new FileReader();

        reader.onload = function (e) {
            basePathLoadGpx(reader.result, '#A52A2A');
        };

        reader.readAsText(file);
    });
}

//버튼 클릭 시 호출되는 핸들러 입니다
function selectOverlay(type) {
    // 그리기 중이면 그리기를 취소합니다
    _drawingManager.cancel();

    // 클릭한 그리기 요소 타입을 선택합니다
    _drawingManager.select(kakao.maps.drawing.OverlayType[type]);
}




function onClickReset() {
    $('#reset').click(function () {
        if (confirm('초기화 할까요?'))
            location.reload();
    });
}

function saveGpxToServer(saveData, apiName) {
    return new Promise((resolve, reject) => {
        const compressedData = LZString.compressToUTF16(saveData);
        let jsonData = {
            apiName: apiName,
            gpxName: _fileName,
            fileExt: _filetype,
            wayPointCount: 0,
            trackPointCount: _gpxTrkseqArray.length,
            distance: _gpxTrkseqArray[_gpxTrkseqArray.length - 1].dist, //Meter
            xmlData: compressedData,
            speed: Number($('#averageV').val())
        };

        $.ajax({
            url: '/api/1.0/saveElevation',
            type: 'POST',
            contentType: 'application/json',
            data: JSON.stringify(jsonData),
            async: true,    //pc 저장과 별개...
            success: function (response) {
                //console.log('Data successfully sent to the server');
                //console.log(response);
                resolve(response.data.fileKey);
            },
            error: function (xhr, status, error) {
                console.log('Error sending data: ' + error);
                reject(error);
            }
        });
    });
}

function onClickSaveGpx() {
    $('#gpxsave').click(function () {
        let data = _drawingManager.getData();
        let len = data[kakao.maps.drawing.OverlayType.POLYLINE].length;

        if (len == 0) {
            alert('경로정보가 없습니다."경로 그리기" 기능으로 경로를 그린 후 저장합니다.');
            return;
        }
        // if(!confirm("고도(높이) 정보를 처리하지 않고 경로를 저장할까요?")) {
        //     return;
        // }

        //구글 높이를 받아오지 않은 경우에도 경로를 저장하기 위한 정보처리
        if (eleFalg == false) {
            _gpxTrkseqArray = [];
            for (let i = 0; i < len; i++) {
            //     let line = pointsToPath(data.polyline[i].points);
            //     let tempArray = line.map(point => ({
            //         lat: point.getLat(), lng: point.getLng(), ele: 0, dist: 0, time: ''
            //     }));
            //     _gpxTrkseqArray.push(...tempArray); // 스프레드 연산자를 사용하여 배열에 추가
                data.polyline[i].points.forEach(point => {
                    let line = new kakao.maps.LatLng(point.y, point.x);
                    _gpxTrkseqArray.push({
                        lat: line.getLat(),
                        lng: line.getLng(),
                        ele: 0,
                        dist: 0,
                        time: ''
                    });
                });
            }
        }

        _fileName = (new Date().getTime() / 1000).toFixed(0);

        let ptDateTime = new Date(BASETIME);

        _gpxTrkseqArray[0].time = (new Date(BASETIME)).toISOString();
        _gpxTrkseqArray[0].dist = 0;

        //시간 = 거리 / 속도
        let speed = Number($('#averageV').val());
        for (let trkptIndex = 1; trkptIndex < _gpxTrkseqArray.length; trkptIndex++) {
            let distance = getDistance(_gpxTrkseqArray[trkptIndex - 1], _gpxTrkseqArray[trkptIndex]);
            _gpxTrkseqArray[trkptIndex].dist = (Number(_gpxTrkseqArray[trkptIndex - 1].dist) + distance).toFixed(2);
            let ptSecond = distance / speed * 3600;
            ptDateTime.setSeconds(ptDateTime.getSeconds() + ptSecond);
            _gpxTrkseqArray[trkptIndex].time = ptDateTime.toISOString();
            _gpxTrkseqArray[trkptIndex].desc = _gpxTrkseqArray[trkptIndex].dist; //누적거리 km
        }
        let saveData = saveGpx(_fileName, Number($('#averageV').val()),
            [], _gpxTrkseqArray);

        saveAs(new Blob([saveData], {
            type: "application/vnd.garmin.gpx+xml"
        }), _fileName + '.' + _filetype);

        saveGpxToServer(saveData, 'saveElevation');
    });
}

//서버에 저장 후 file를 연결한다. gpsdata에서 apiname=linkElevation 저장하고, 10분 이내인 것을 사용한다.
function onClickOpenGiljabi() {
    $('#openGiljabi').click(function () {
        let data = _drawingManager.getData();
        let len = data[kakao.maps.drawing.OverlayType.POLYLINE].length;

        if (len == 0) {
            alert('경로정보가 없습니다."경로 그리기" 기능으로 경로를 그린 후 저장합니다.');
            return;
        }

        if (eleFalg == false) {
            _gpxTrkseqArray = [];
            for (let i = 0; i < len; i++) {
                data.polyline[i].points.forEach(point => {
                    let line = new kakao.maps.LatLng(point.y, point.x);
                    _gpxTrkseqArray.push({
                        lat: line.getLat(),
                        lng: line.getLng(),
                        ele: 0,
                        dist: 0,
                        time: ''
                    });
                });
            }
        }

        _fileName = (new Date().getTime() / 1000).toFixed(0);

        let ptDateTime = new Date(BASETIME);

        _gpxTrkseqArray[0].time = (new Date(BASETIME)).toISOString();
        _gpxTrkseqArray[0].dist = 0;

        //시간 = 거리 / 속도
        let speed = Number($('#averageV').val());
        for (let trkptIndex = 1; trkptIndex < _gpxTrkseqArray.length; trkptIndex++) {
            let distance = getDistance(_gpxTrkseqArray[trkptIndex - 1], _gpxTrkseqArray[trkptIndex]);
            _gpxTrkseqArray[trkptIndex].dist = (Number(_gpxTrkseqArray[trkptIndex - 1].dist) + distance).toFixed(2);
            let ptSecond = distance / speed * 3600;
            ptDateTime.setSeconds(ptDateTime.getSeconds() + ptSecond);
            _gpxTrkseqArray[trkptIndex].time = ptDateTime.toISOString();
            _gpxTrkseqArray[trkptIndex].desc = _gpxTrkseqArray[trkptIndex].dist; //누적거리 km
        }
        let saveData = saveGpx(_fileName, Number($('#averageV').val()),
            [], _gpxTrkseqArray);

        saveGpxToServer(saveData, 'linkElevation').then(function (uuid) {
            //새창열기
            window.open('/giljabi.html?elevation=linkElevation&fileid=' + uuid, uuid);
        }).catch(error => {
            console.error('Error:', error);
        });

    });
}

$(document).ready(function() {
    BASETIME = setBaseTimeToToday(BASETIME);
    $(document).tooltip();

    initMap();

    initDrawing();

    onClickFileInput();

    onClickAll100();    //전체 버튼을 클릭하면 100개 산 전체 GPX파일을 받아온다.

    onSelectByCombo();  //combo100에서 선택한 산의 GPX파일을 받아온다.

    //POI정보를 조회
    addListener();

    onClickGetElevation();

    onClickBaseInput(); //배경으로 사용할 GPX파일을 선택한다.

    onClickReset();

    onClickSaveGpx();

    onClickOpenGiljabi();
});

