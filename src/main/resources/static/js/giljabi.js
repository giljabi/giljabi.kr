/**
 * 2018.10.26 gpx to tcx Project start....
 * Author : park namjun, eahn.park@gmail.com
 *
 https://www.topografix.com/GPX/1/1
 Element: gpx
 Complex Type: gpxType
 Complex Type: metadataType
 Complex Type: wptType
 Complex Type: rteType
 Complex Type: trkType
 Complex Type: extensionsType
 Complex Type: trksegType
 Complex Type: copyrightType
 Complex Type: linkType
 Complex Type: emailType
 Complex Type: personType
 Complex Type: ptType
 Complex Type: ptsegType
 Complex Type: boundsType
 Simple Type: latitudeType
 Simple Type: longitudeType
 Simple Type: degreesType
 Simple Type: fixType
 Simple Type: dgpsStationType

 */

let _newPosition;

let _map;
let _gpxMetadata = {};		//gpx/metadata
let _gpxTrkseqArray = [];		//gpx/trk/trkseq
let _eleArray = [];		//경로의 높이정보
let _chkRoute = false;
let _lastPoint;		//경로탐색을 위한 마지막 포인트
let _routeMarkerArray = [];	//경로탐색을 위한 마커
let _polyline = [];	//2개 이상의 경로를 배열로 보관
//let _place;	//장소 검색 객체
//let contentNode = document.createElement('div'); // 커스텀 오버레이의 컨텐츠 엘리먼트 입니다
let _keywordMarker = new kakao.maps.Marker();
let _mapLevel = 3;	//검색 후 지도 스케일
let _markings = [];	//고도차트를 그리기 위한 데이터리스트
let waypointSortByDistance = [];	//웨이포인트를 거리별로 정열된 배열
let _filetype = '';	//gpx, tcx 구분

//'2022-01-01T00:00:00Z';
let BASETIME = new Date('2022-01-01T00:00:00Z');
let steepPoints = [];

let _place;
let poiCategory = '';
let poiCategoryMarkers = [];
let placeOverlay = new kakao.maps.CustomOverlay({zIndex:1});
let contentNode = document.createElement('div'); // 커스텀 오버레이의 컨텐츠 엘리먼트 입니다


//POI마커를 모두 제거
function removeCategryMarker() {
    poiCategoryMarkers.forEach(function (marker) {
        marker.setMap(null);
    });
    poiCategoryMarkers = [];
}

function displayPlaceInfo (place) {
    let address = place.road_address_name ? `
        <span title="${place.road_address_name}">${place.road_address_name}</span>
        <span class="jibun" title="${place.address_name}">(지번: ${place.address_name})</span>
    ` : `
        <span title="${place.address_name}">${place.address_name}</span>
    `;

    let content = `
        <div class="placeinfo">
            <a class="title" href="${place.place_url}" target="_blank" title="${place.place_name}">${place.place_name}</a>
            ${address}
            <span class="tel">${place.phone}</span>
        </div>
        <div class="after"></div>
    `;

    contentNode.innerHTML = content;
    placeOverlay.setPosition(new kakao.maps.LatLng(place.y, place.x));
    placeOverlay.setMap(_map);
}

//var imageSrc = 'http://t1.daumcdn.net/localimg/localimages/07/mapapidoc/places_category.png'; // 마커 이미지 url, 스프라이트 이미지를 씁니다
function addCategoryMarker(position) {
    let imageSrc = '/images/' + poiCategory + '.gif';
    let imageSize = new kakao.maps.Size(22, 24);  // 마커 이미지의 크기
    let markerImage = new kakao.maps.MarkerImage(imageSrc, imageSize);
    let marker = new kakao.maps.Marker({
        position: position, // 마커의 위치
        image: markerImage
    });

    marker.setMap(_map); // 지도 위에 마커를 표출합니다
    poiCategoryMarkers.push(marker);  // 배열에 생성된 마커를 추가합니다

    return marker;
}

// 카테고리 검색을 요청하는 함수입니다
function searchPlaces() {
    if(poiCategory != '') {
        // 커스텀 오버레이를 숨깁니다
        placeOverlay.setMap(null);

        removeCategryMarker();
        _place.categorySearch(poiCategory, placesSearchCallback, {useMapBounds:true});
    }
}

// 장소검색이 완료됐을 때 호출되는 콜백함수 입니다
function placesSearchCallback(data, status, pagination) {
    if (status === kakao.maps.services.Status.OK) {
        //console.log('success')
        displayPlaces(data);
    } else if (status === kakao.maps.services.Status.ZERO_RESULT) {
        // 검색결과가 없는경우 해야할 처리가 있다면 이곳에 작성해 주세요
    } else if (status === kakao.maps.services.Status.ERROR) {
        // 에러로 인해 검색결과가 나오지 않은 경우 해야할 처리가 있다면 이곳에 작성해 주세요
    }
}

// 지도에 마커를 표출하는 함수입니다
function displayPlaces(places) {
    places.forEach(place => {
        const marker = addCategoryMarker(new kakao.maps.LatLng(place.y, place.x));
        kakao.maps.event.addListener(marker, 'click', () => displayPlaceInfo(place));
    });
}

$(document).ready(function () {
    BASETIME = setBaseTimeToToday(BASETIME);

    $(document).tooltip();

    //지도초기화
    let container = document.getElementById('map');
    let options = {
        center: getLocation(),	//사용자 위치 또는 서울시청
        level: 8 //default 8
    };

    _map = new kakao.maps.Map(container, options);
    _place = new kakao.maps.services.Places(_map);

    // 일반 지도와 스카이뷰로 지도 타입을 전환할 수 있는 지도타입 컨트롤을 생성합니다
    _map.addControl(new kakao.maps.MapTypeControl(), kakao.maps.ControlPosition.TOPRIGHT);

    $(".mapType").change(function () {
        for (var type in _mapTypes) {
            _map.removeOverlayMapTypeId(_mapTypes[type]);
        }
        if ($("#chkTerrain").is(":checked")) {
            _map.addOverlayMapTypeId(_mapTypes.terrain);
        }
    });

    //편의점. 숙박 POI
    $("input[name=daumpoi]").change(function() {
        poiCategory = $(this).val();
        removeCategryMarker();
        searchPlaces();
    });

    contentNode.className = 'placeinfo_wrap';

    // 커스텀 오버레이 컨텐츠를 설정합니다
    placeOverlay.setContent(contentNode);

     kakao.maps.event.addListener(_map, 'dragend', function() {
         removeCategryMarker()
         searchPlaces();
	});

     kakao.maps.event.addListener(_map, 'zoom_changed', function() {
	    let level = _map.getLevel();
	    //console.log('현재 지도 레벨은 ' + level + '입니다');
        searchPlaces();
	});

    let route = [];	//route api에서 사용하는 시작과 끝 위치정보
    kakao.maps.event.addListener(_map, 'click', function (mouseEvent) {
        if (_chkRoute) {	//waypoint ?, cycling/hiking ?
            if (route.length === 0)
                makeMarkerRoute(mouseEvent.latLng, 'daumstart.png');

            route.push(mouseEvent.latLng);
            if (route.length === 2) {
                _lastPoint = route[1];	//마지막 포인트는 계속 저장한다
                getRoute(route);
                route.length = 0;
                route[0] = _lastPoint;	//마지막 포인트는 저장
            }
        } else {
            //giljai는 소숫점 6자리만 사용
            let lat = Number(mouseEvent.latLng.getLat().toFixed(6));
            let lng = Number(mouseEvent.latLng.getLng().toFixed(6));
            let info = makeWaypointObject(new kakao.maps.LatLng(lat, lng));
            addWaypoint(info);
            //console.log('kakao.maps.event.addListener click:' + info.position.toString());	//icon 좌표
        }
    });

    function addWaypoint(info) {
        info.sym = info.sym === '' ? 'Generic' : info.sym;	//Symbol이 없는 경우
        let myWayPoint = new Waypoint(_map, info.position, info.name, info.uid, info.sym);
        //position을 waypoint와 중복해서 사용하는 이유는 overlay에서 정보를 가져오기가 어렵네...
        _wayPointArray.push({
            uid: info.uid,
            position: info.position,
            customoverlay: myWayPoint,
            waypointname: info.name,
            sym: info.sym,
            ele: info.ele
        });
    }

    $('.waypointIcon').click(function () {
        _pointIcon = this.id;
        $('#selectWaypointIcon').attr('src', 'images/' + _pointIcon + '.png');

        let name = $('#waypointName').val().toLowerCase();
        //console.log(this.id + ',' + _waypointIcons.indexOf(name));
        //사용자가 입력한 웨이포인트 이름은 변경하지 않는다..
        if (_waypointIcons.indexOf(name) >= 0)
            $('#waypointName').val(capitalizeFirstLetter(_pointIcon));
    });

    //combo에서 direction 타입이 웨이포인트인지, 경로탐색인지 구분하느 플래그
    $('#direction').change(function () {
        let test = $("#direction option:selected").val();
        _chkRoute = test !== "waypoint";
        //console.log('direction flag:' + _chkRoute);
    });

    //=======================================================================
    //file loading....
    $('#fileInput').change(function () {
        if (_firstFileOpenFlag) {
            alert('이미 기본 파일이 열려 있습니다.');
            return;
        } else {
            let file = document.getElementById('fileInput').files[0];
            _uploadFilename = file.name.substring(0, file.name.lastIndexOf('.'));			//파일명
            _fileExt = file.name.substring(file.name.lastIndexOf('.') + 1).toLowerCase();	//확장자
            _firstFileOpenFlag = true;

            let reader = new FileReader();
            reader.readAsText(file);

            reader.onload = function (e) {
                fileLoadAndDraw(reader.result);
                //console.log('fileInput filename :' + _uploadFilename);	//필요하면 디버깅으로...
            };
            $('#fileInput').prop('disabled', true);
        }

        //파일로드 후 광고 삭제
        chartPlotAdView(false);
    });

    function fileLoadAndDraw(xml) {
        let readXmlfile = $($.parseXML(xml.replace(/&/g, "&amp;")));
        //console.log('fileLoadAndDraw:' + readXmlfile);

        if (_fileExt == 'gpx') {
            loadGpx(readXmlfile);
        } else if (_fileExt == 'tcx') {
            loadTcx(readXmlfile); //gpx 포맷을 끝내고 진행...
        }

        drawPolyline(_trkPoly); //경로를 그린다.

        //시작과 끝 표시
        makeMarkerPoint(_map, 'start', _gpxTrkseqArray[0]);
        makeMarkerPoint(_map, 'end', _gpxTrkseqArray[_gpxTrkseqArray.length - 1]);

        let midPoint = _gpxTrkseqArray[parseInt(_gpxTrkseqArray.length / 2)];
        _map.setCenter(new kakao.maps.LatLng(midPoint.lat, midPoint.lng)); //중심점을 경로상의 중간을 설정한다.
        _map.setLevel(10);

        getWaypointInfo();
    }

    /**
     * gpx file loading
     * @param loadFile
     */
    function loadGpx(loadFile) {
        //정상적인 포맷에서 필요한 정보들...
        _gpxMetadata.author = loadFile.find('gpx').find('metadata').find('author').text();
        _gpxMetadata.name = loadFile.find('gpx').find('metadata').find('name').text();

        if (_gpxMetadata.name == "")
            $('#gpx_metadata_name').val(_uploadFilename);
        else
            $('#gpx_metadata_name').val(_gpxMetadata.name);

        _gpxMetadata.desc = loadFile.find('gpx').find('metadata').find('desc').text();
        _gpxMetadata.speed = loadFile.find('gpx').find('metadata').find('speed').text();
        if (_gpxMetadata.speed == "")
            $('#averageV').val('15');
        else
            $('#averageV').val(_gpxMetadata.speed);

        //gpx파일의 waypoint 정보
        $.each(loadFile.find('gpx').find('wpt'), function () {
            let item = new GpxWaypoint(
                Number($(this).attr('lat')),
                Number($(this).attr('lon')),
                Number($(this).find('ele')),
                $(this).find('name').text(),
                $(this).find('desc').text(),
                $(this).find('type').text(),
                getIconString($(this).find('sym').text())
            ); //console.log('loadGpx item:' + item.toString());
            addWaypoint(item);
        });

        //경로정보
        $.each(loadFile.find('gpx').find('trk').find('trkseg').find('trkpt'), function () {
            let trackPoint = new Point3D(
                Number($(this).attr('lat')),
                Number($(this).attr('lon')),
                Number($(this).find('ele').text()),
                Number($(this).find('dist').text()),
                Number($(this).find('time').text())
            );
            _gpxTrkseqArray.push(trackPoint);
            _trkPoly.push(new kakao.maps.LatLng(trackPoint.lat, trackPoint.lng));
        });
        $("input[type='radio'][name='filetype'][value='gpx']").prop("checked", true);

        //steepPoints = findSteepSlopes(_gpxTrkseqArray);
    }

    /**
     * tcx file loading
     * @param loadFile
     */
    function loadTcx(loadFile) {
        _gpxMetadata.author = loadFile.find('TrainingCenterDatabase').find('Folders').find('Courses').find('CourseFolder').find('CourseNameRef').find('Author').text();
        _gpxMetadata.name = loadFile.find('TrainingCenterDatabase').find('Courses').find('Course').find('Name').text();

        if (_gpxMetadata.name == "")
            $('#gpx_metadata_name').val(_uploadFilename);
        else
            $('#gpx_metadata_name').val(_gpxMetadata.name);

        //_gpxMetadata.desc = loadFile.find('gpx').find('metadata').find('desc').text();
        _gpxMetadata.speed = loadFile.find('TrainingCenterDatabase').find('Courses').find('Course').find('Speed').text();
        if (_gpxMetadata.speed == "")
            $('#averageV').val('15');
        else
            $('#averageV').val(_gpxMetadata.speed);

        //waypoint가 있는경우
        $.each(loadFile.find('CoursePoint'), function () {
            let item = new GpxWaypoint(
                $(this).find('LatitudeDegrees').text(),
                $(this).find('LongitudeDegrees').text(),
                0,
                $(this).find('Name').text(),		//웨이포인트 이름
                '',			//웨이포인트 설명
                '',			//sym 설명
                $(this).find('PointType').text());//console.log('loadGpx item:' + item.toString());
            //gpx파일 로딩시 waypoint가 있으면 그려준다.
            addWaypoint(item);
        });

        //경로
        //$.each(loadFile.find('gpx').find('trk').find('trkseg').find('trkpt'), function () {
        $.each(loadFile.find('Trackpoint'), function () {
            let trackPoint = new Point3D(
                Number($(this).find('LatitudeDegrees').text()),
                Number($(this).find('LongitudeDegrees').text()),
                Number($(this).find('AltitudeMeters').text()),
                Number($(this).find('DistanceMeters').text()),
                ''
            );

            _gpxTrkseqArray.push(trackPoint);
            _trkPoly.push(new kakao.maps.LatLng(trackPoint.lat, trackPoint.lng));
        });

        $("input[type='radio'][name='filetype'][value='tcx']").prop("checked", true);
    }

    //참고 http://www.flotcharts.org/flot/examples/tracking/index.html
    //위치정보 http://www.flotcharts.org/flot/examples/interacting/index.html
    //zoom http://www.flotcharts.org/flot/examples/selection/index.html
    //http://www.flotcharts.org/flot/examples/annotating/index.html
    //http://www.flotcharts.org/flot/examples/basic-options/index.html
    //plot = $.plot("#elevationImage", [{ data: _eleArray}, { data: horiArray}]

    let plot;
    function drawPlot() {
        $('#elevationImage').empty();
        let updateLegendTimeout = null;
        let latestPosition = null;
        let cursorMarker = new kakao.maps.Marker();

        let numbers = [];
        for(let i = 0; i < _gpxTrkseqArray.length; i++) {
            numbers.push(_gpxTrkseqArray[i].ele);
        }
        let maxAlti = Math.max(...numbers);
        let minAlti = Math.min(...numbers);

        plot = $.plot("#elevationImage",
            [{
                data: _eleArray,
                color: 'green',
                //lines: {show: true},
                //points: {show: false},
            }], {
                series: {lines: {show: true, shadowSize: 0, lineWidth: 2}},
                crosshair: {mode: "xy"},
                grid: {
                    clickable: true,
                    hoverable: true,
                    show: true,
                    aboveData: true,
                    //selection: {
                    //    mode: "xy"
                    //},
                    markings: [], //세로선(water, summit)을 그리기 위한 데이터
                }, hooks: { draw: [addImageIcons] },
                yaxis: {
                    min: minAlti * 0.7,
                    max: maxAlti * 1.2,
                    } //위/아래 여백
            });

        //차트에서 마우스의 움직임이 있으면 지도상에 마커를 이동시킨다
        function updateLegend() {
            updateLegendTimeout = null;
            let pos = latestPosition;
            let dataset = plot.getData();
            let seriesIndex = 0;
            let series = dataset[0].data;
            for (seriesIndex = 0; seriesIndex < series.length; seriesIndex++) {
                if (series[seriesIndex][0] >= pos.x) {
                    break;
                }
            }
            //차트 오른쪽에서 마우스가 접근하면 pos.x보다 큰 값이 없어서 seriesIndex가 증가하게 되어 가장 오른쪽 값으로 사용함
            if(seriesIndex > series.length - 1) {
                seriesIndex = series.length - 1;
            }
            if(seriesIndex < 0)
                seriesIndex = 0;

            cursorMarker.setMap(null);	//마커를 삭제하고

            //고도차트에서 마우스를 따라 움직이는 지도상의 마커
            if(_gpxTrkseqArray.length == 0)
                return;
            cursorMarker = new kakao.maps.Marker({
                position: new kakao.maps.LatLng(_gpxTrkseqArray[seriesIndex].lat, _gpxTrkseqArray[seriesIndex].lng)
            });
            cursorMarker.setMap(_map);
        }

        //고도정보
        $("<div id='tooltip'></div>").css({
            position: "absolute",
            display: "none",
            border: "1px solid #fdd",
            padding: "2px",
            "background-color": "#fee",
            opacity: 0.80,
            "font-size": "12px"
        }).appendTo("body");

        //차트에서 마우스의 움직임이 있으면....
        $("#elevationImage").bind("plothover", function (event, pos, item) {
            latestPosition = pos;
            if (!updateLegendTimeout) {
                updateLegendTimeout = setTimeout(updateLegend, 50);
            }
            //console.log('item.dataIndex:' + item.dataIndex);

            if(item != null) {
                //기울기를 표시, 왼쪽 2개, 오른쪽 2개를 비교한다.
                if (item.dataIndex > 2 && item.dataIndex < _gpxTrkseqArray.length - 2) {
                    let leftDistance = (_gpxTrkseqArray[item.dataIndex - 2].dist - _gpxTrkseqArray[item.dataIndex - 1].dist) / 2;
                    let rightDistance = (_gpxTrkseqArray[item.dataIndex + 1].dist - _gpxTrkseqArray[item.dataIndex + 2].dist) / 2;
                    //왼쪽 2개의 중앙에서 오른쪽 중앙의 거리
                    let distance = (Math.abs(leftDistance) + Math.abs(rightDistance) +
                        Math.abs(_gpxTrkseqArray[item.dataIndex + 1].dist - _gpxTrkseqArray[item.dataIndex - 1].dist)) * 1000;

                    let leftElevation = (_gpxTrkseqArray[item.dataIndex - 2].ele + _gpxTrkseqArray[item.dataIndex - 1].ele) / 2;
                    let rightElevation = (_gpxTrkseqArray[item.dataIndex + 1].ele + _gpxTrkseqArray[item.dataIndex + 2].ele) / 2;
                    let elevationChange = rightElevation - leftElevation;

                    let slope = calculateSlope(distance, elevationChange);
                    //console.log('slope:' + slope + ', elevation:' + elevationChange + ', distance:' + distance);

                    let x = item.datapoint[0].toFixed(1),
                        y = item.datapoint[1].toFixed(0);
                    //console.log(x + ' / ' + y + 'item:' + item.toString());

                    let backgroundColor; // 기본 색상
                    if (Math.abs(slope) >= 30) {
                        backgroundColor = "#FF0000"; // 높은 값에 대한 색상
                    } else if (Math.abs(slope) >= 20 && Math.abs(slope) < 30) {
                        backgroundColor = "#21ECFF"; // 중간 값에 대한 색상
                    } else if (Math.abs(slope) >= 10 && Math.abs(slope) < 20) {
                        backgroundColor = "#F6A6FF"; // 중간 값에 대한 색상
                    } else
                        backgroundColor = "#FDDDFF"

                    $("#tooltip").html(x + '/' + y + ', ' + slope + '%')
                        .css({top: item.pageY+5, left: item.pageX+5, "background-color": backgroundColor})
                        .fadeIn(100);
                } else {
                    $("#tooltip").hide();
                }
            }
        });

        /* zoom을 사용하려는데...잘 안되네..
        $("#elevationImage").bind("plotselected", function (event, ranges) {
            var plot = $.plot("#placeholder", [data], $.extend(true, {}, options, {
                xaxis: { min: ranges.xaxis.from, max: ranges.xaxis.to },
                yaxis: { min: ranges.yaxis.from, max: ranges.yaxis.to }
            }));
        });
        $("#elevationImage").bind("plotunselected", function (event) {
            $("#selection").text("");
        });
*/
        //웨이포인트 클릭과 같은 위치로 이동기능 추가, item.dataIndex를 사용할 수 있음
        $("#elevationImage").bind("plotclick", function (event, pos, item) {
            if(item != null)
                goCenter(_gpxTrkseqArray[item.dataIndex].lat, _gpxTrkseqArray[item.dataIndex - 1].lng,5);
        });

        //웨이포인트의 water, summit 아이콘
        function addImageIcons(plot, canvascontext) {
            _markings.forEach(function (marking) {
            //for (let i = 0; i < _markings.length; i++) {
                //console.log(_markings[i].y + ', ' + maxAlti);
                let img = new Image();
                img.onload = function() {
                        let o = plot.pointOffset({x: marking.x, y: maxAlti});
                        canvascontext.drawImage(img, o.left - img.width / 2, o.top - img.height);
                    }
                img.src = '/images/'+ marking.sym +'.png'; // 이미지 경로
            });
        }

    }

    $('#reset').click(function () {
        if (confirm('초기화 할까요?'))
            location.href = '/giljabi.html';
    });

    $('#waypointinfo').click(function () {
        if (_wayPointArray.length === 0) {
            alert('웨이포인트가 없습니다. 경로상에 웨이포인트가 1개 이상 있어야 합니다.');
        } else
            getWaypointInfo();
    });

    //gpx파일을 병합한다.
    $('#mergeInput').change(function () {
        alert('준비중입니다.');
        return;
        /*
                if (!_firstFileOpenFlag) {
                    alert('GPX/TCX 파일을 먼저 선택해야 병합 파일을 열 수 있습니다.');
                    return;
                }

                let file = document.getElementById('mergeInput').files[0];
                _fileExt = file.name.substring(file.name.lastIndexOf('.') + 1);
                let reader = new FileReader();
                reader.onload = function (e) {
                    mergeObject(reader.result);
                };
                reader.readAsText(file);*/
    });

    //직전의 경로에 경로를 이어 붙힌다
    function mergeObject(xml) {
        let _xmlData = $($.parseXML(xml));
        /*
        let endPosition = new kakao.maps.LatLng(
                  _gpxTrkseqArray[_gpxTrkseqArray.length - 1].lat
                , _gpxTrkseqArray[_gpxTrkseqArray.length - 1].lon);
        */
        let endPosition = _gpxTrkseqArray[_gpxTrkseqArray.length - 1];

        let nearDistance = 0;	//최단거리
        let currDistance = 0;	//현재거리
        let startIndex = 0;
        let polyline = new Array();	//추가된 선을 그린다.
        polyline.push(new kakao.maps.LatLng(
            _gpxTrkseqArray[_gpxTrkseqArray.length - 1].lat
            , _gpxTrkseqArray[_gpxTrkseqArray.length - 1].lng)); //마지막 위치에서 연결

        if (_fileExt == 'gpx') {
            //직전 경로의 마지막 위치와 현재 업로드된 경로에서 가장 가까운 위치를 찾아서 시작 위치로 한다. 이게 필요할까????
            let trkSeqArray = _xmlData.find('trkpt');
            for (let i = 0; i < trkSeqArray.length; i++) {
                currDistance = getDistance(new Point3D(endPosition.lat, endPosition.lng),
                    new Point3D($(trkSeqArray[i]).attr('lat'), $(trkSeqArray[i]).attr('lon'), 0, 0, ''));
                /*                console.info(
                                    endPosition,
                                    $(trkSeqArray[i]).attr('lat'), $(trkSeqArray[i]).attr('lon'),
                                    currDistance);*/
                if (currDistance <= nearDistance) {
                    nearDistance = currDistance;
                    startIndex = i;
                }
            }

            //병합할 경로에서 필요없는 부분을 제거한다.
            trkSeqArray.splice(0, startIndex);
            //console.log("nearDistance:" + nearDistance +', index:' + startIndex);
            $.each(trkSeqArray, function () {
                //trkpt
                let trackPoint = new Point3D(
                    $(this).attr('lat'),
                    $(this).attr('lon'),
                    $(this).find('ele').text(), 0, '');

                _gpxTrkseqArray.push(trackPoint);
                polyline.push(new kakao.maps.LatLng(trackPoint.lat, trackPoint.lng));
            });

            //waypoint가 있는경우 모두 넣어준다. 잘라내기 어려워서... 입력 후 손으로 지우는게 편함.....
            ////var wpt = _xmlData.find('gpx').find('wpt');
            $.each(_xmlData.find('gpx').find('wpt'), function () {
                let item = GpxWaypoint(
                    $(this).attr('lat'),
                    $(this).attr('lon'),
                    $(this).find('ele').text(),
                    $(this).find('name').text(),
                    $(this).find('desc').text(),
                    $(this).find('type').text(),
                    getIconString($(this).find('sym').text())
                );
                //gpx파일 로딩시 waypoint가 있으면 그려준다.
                addWaypoint(item);
            });
        } else if (_fileExt == 'tcx') {	//tcx를 빼고 할까????
            var trkSeqArray = _xmlData.find('Trackpoint');
            for (var i = 0; i < trkSeqArray.length; i++) {
                var temp = $(trkSeqArray[i]);
                currDistance = getDistanceFromLatLon(endPosition.getLat(), endPosition.getLng()
                    , temp.find('LatitudeDegrees').text()
                    , temp.find('LongitudeDegrees').text());
                if (currDistance <= nearDistance) {
                    nearDistance = currDistance;
                    startIndex = i;
                }
            }
            //병합할 경로에서 필요없는 부분을 제거한다.
            trkSeqArray.splice(0, startIndex - 1);
            //console.log("nearDistance:" + nearDistance +', index:' + startIndex);
            $.each(trkSeqArray, function () {
                var trkpt = getGpxTrk($(this).find('LatitudeDegrees').text()
                    , $(this).find('LongitudeDegrees').text()
                    , $(this).find('AltitudeMeters').text());
                _gpxTrkseqArray.push(trkpt);
                polyline.push(new kakao.maps.LatLng($(this).find('LatitudeDegrees').text()
                    , $(this).find('LongitudeDegrees').text()));
            });

            //waypoint가 있는경우
            $.each(_xmlData.find('CoursePoint'), function () {
                var wptitem = new Object();
                wptitem.uid = _microTime++;
                wptitem.position = new kakao.maps.LatLng($(this).find('LatitudeDegrees').text()
                    , $(this).find('LongitudeDegrees').text());
                wptitem.ele = '';
                wptitem.name = $(this).find('Name').text();		//웨이포인트 이름
                wptitem.desc = '';		//웨이포인트 설명
                wptitem.type = '';		//sym 설명
                wptitem.sym = $(this).find('PointType').text();

                //gpx파일 로딩시 waypoint가 있으면 그려준다.
                addWaypoint(wptitem);
            });

        }

        //끝점만 표시
        makeMarkerPoint(_map, 'end', _gpxTrkseqArray[_gpxTrkseqArray.length - 1]);
        drawPolyline(polyline);

        drawPlot();
    }

    function drawPolyline(polyline) {
        // 지도에 표시할 선을 생성합니다
        let lineStyle = new kakao.maps.Polyline({
            path: polyline, // 선을 구성하는 좌표배열
            strokeWeight: 5, // 선의 두께
            strokeColor: '#FF0000', // 선의 색깔
            strokeOpacity: 0.7, // 선의 불투명도, 1에서 0 사이의 값이며 0에 가까울수록 투명
            strokeStyle: 'solid' // 선의 스타일
        });
        // 지도에 선을 표시합니다
        lineStyle.setMap(_map);
        _polyline.push(lineStyle);
    }

    //경로 뒤집기...
    $('#reverse').click(function () {
        //console.info('reverse');

        if (_gpxTrkseqArray.length == 0) {
            alert('경로가 없습니다.');
            return;
        }

        //경로, 웨이포인트 재계산 해야 함
        _gpxTrkseqArray = _gpxTrkseqArray.reverse();
        _wayPointArray = _wayPointArray.reverse();

        getWaypointInfo();
    });


    /**
     * 시작위치에서 목표위치까지의 경로를 찾는다..
     * @param route
     */
    function getRoute(route) {
        $('#blockingAds').show();
        let polyline = new Array();	//추가된 선을 그린다.

        let param = {
            start: {
                lat: route[0].getLat(),
                lng: route[0].getLng()
            },
            end: {
                lat: route[1].getLat(),
                lng: route[1].getLng()
            },
            direction: $('#direction option:selected').val()
        };

        $.ajax({
            type: 'post',
            url: '/api/1.0/route',
            dataType: 'json',
            contentType: 'application/json; charset=UTF-8;',
            data: JSON.stringify(param),
            async: true,
            complete: function () {

            },
            success: function (response, status) {
                //status : success
                if (response.status === 0) {	//정상
                    chartPlotAdView(false);

                    //_gpxTrkseqArray = []; //chkRoute의 체크 유무에 따라 초기화를 결정해야 한다.
                    //eleFalg = true;
                    ///////var jsonList = JSON.parse(response.data);
                    $.each(response.data, function () {
                        let trackPoint = new Point3D(
                            $(this).attr('lat'),
                            $(this).attr('lng'),
                            $(this).attr('ele'),
                            0, '');
                        _gpxTrkseqArray.push(trackPoint);
                        polyline.push(new kakao.maps.LatLng(trackPoint.lat, trackPoint.lng));
                    });
                    makeMarkerRoute(_lastPoint, 'daumend.png');
                    //makeMarkerRoute(_gpxTrkseqArray[0]);
                    //makeMarkerRoute(_gpxTrkseqArray[_gpxTrkseqArray.length - 1]);
                    getWaypointInfo();
                    drawPolyline(polyline);
                } else {
                    alert(response.status + ',' + response.message);
                }
                $('#blockingAds').hide();
            }
        });
    }

    //경로탐색의 마크를 사용
    function makeMarkerRoute(latlon, image) {
        let currentMarkerPosition = _routeMarkerArray.length;
        let markerObject = {};
        markerObject.currPosition = _gpxTrkseqArray.length;
        markerObject.prevPosition = _routeMarkerArray.length > 0 ? _routeMarkerArray[_routeMarkerArray.length - 1].currPosition : 0;

        let imageSize = new kakao.maps.Size(50, 45);
        let startOption = {
            offset: new kakao.maps.Point(35, 0) // 출발 마커이미지에서 마커의 좌표에 일치시킬 좌표를 설정, 기본값은 이미지의 가운데 아래
        };
        let markerImage = new kakao.maps.MarkerImage('/images/' + image, imageSize, startOption);

        let marker = new kakao.maps.Marker({
            position: latlon,
            image: markerImage
        });
        marker.setMap(_map);
        markerObject.marker = marker;
        _routeMarkerArray.push(markerObject);

        kakao.maps.event.addListener(marker, 'click', function () {
            if (_routeMarkerArray.length - 1 != currentMarkerPosition) {
                alert('삭제는 마지막 경로부터 가능합니다.');
                return;
            }
            if (!_chkRoute) {
                alert('웨이포인트 상태에서는 경로삭제가 안됩니다.');
                return;
            }

            markerObject.marker.setMap(null);	//Marker 삭제
            _routeMarkerArray.splice(_routeMarkerArray.length - 1);
            _gpxTrkseqArray.splice(markerObject.prevPosition, markerObject.currPosition)

            _polyline[_polyline.length - 1].setMap(null);
            if (_polyline.length == 1) {
                chartPlotAdView(true);

                route = [];//[0] = route[1];
                _polyline = [];	//전체 삭제
                for (let i = 0; i < _routeMarkerArray.length; i++)
                    _routeMarkerArray[i].marker.setMap(null);

                drawPlot(); //차트 초기화
                _routeMarkerArray = [];
                _gpxTrkseqArray = [];//모든 경로 삭제
                _eleArray = [];
            } else {
                _polyline.splice(_polyline.length - 1, 1);
                let temp = _gpxTrkseqArray[_gpxTrkseqArray.length - 1];
                route[0] = new kakao.maps.LatLng(temp.lat, temp.lng);
                getWaypointInfo();
            }
        });
    }

    $('#waypointexcelsave').click(function (e) {
        if (_wayPointArray.length === 0) {
            alert('웨이포인트가 없습니다. 경로와 웨이포인트가 1개 이상 있어야 합니다.');
            return;
        }
        $('#blockingAds').show();

        excelFileExport($('#gpx_metadata_name').val(),
            getWaypointToExcel(waypointSortByDistance));

        $('#blockingAds').hide();
    });

    //날짜는 계산하지 않고 시간만 사용
    function convertSecondsToDaysHoursMinutes(seconds) {
        const hours = Math.floor(seconds / 3600);
        seconds -= hours * 3600;
        let minutes = Math.floor(seconds / 60);

        return String(hours).padStart(2, '0') + ':' + String(minutes).padStart(2, '0');
    }

//Waypoint info, 화면 우측에 보이는 정보
    function getWaypointInfo() {
        if(_gpxTrkseqArray.length == 0)
            return;

        let wpt = [];
        for (let i = 0; i < _wayPointArray.length; i++) {
            wpt.push({
                lat: _wayPointArray[i].position.getLat(),
                lng: _wayPointArray[i].position.getLng(),
                name: _wayPointArray[i].waypointname,
                sym: _wayPointArray[i].sym
            });
        }
        waypointSortByDistance = makeWaypointInfo(wpt);

        _gpxTrkseqArray[0].time = (new Date(BASETIME)).toISOString();
        _gpxTrkseqArray[0].dist = 0;
        let ptDateTime = new Date(BASETIME);
        //시간 = 거리 / 속도
        let speed = Number($('#averageV').val());
        for (let trkptIndex = 1; trkptIndex < _gpxTrkseqArray.length; trkptIndex++) {
            let distance = getDistance(_gpxTrkseqArray[trkptIndex - 1], _gpxTrkseqArray[trkptIndex]);
            _gpxTrkseqArray[trkptIndex].dist = Number((Number(_gpxTrkseqArray[trkptIndex - 1].dist) + distance).toFixed(2));
            let ptSecond = distance / speed * 3600;
            ptDateTime.setSeconds(ptDateTime.getSeconds() + ptSecond);
            _gpxTrkseqArray[trkptIndex].time = ptDateTime.toISOString();
            //console.log(_gpxTrkseqArray[trkptIndex]);
        }

        ptDateTime = new Date(BASETIME);
        for (let wayIndex = 0; wayIndex < waypointSortByDistance.length; wayIndex++) {
            //console.info(waypointSortByDistance[wayIndex]);
            let endTime = new Date(_gpxTrkseqArray[waypointSortByDistance[wayIndex].index].time);
            let diff = endTime - ptDateTime;
            //diff를 시간, 분으로 변환
            waypointSortByDistance[wayIndex].laptime = convertSecondsToDaysHoursMinutes(diff / 1000);

            //Garmin에서는 사용하면 에러...excel저장에서 사용됨
            waypointSortByDistance[wayIndex].time = _gpxTrkseqArray[waypointSortByDistance[wayIndex].index].time;
            waypointSortByDistance[wayIndex].distance = _gpxTrkseqArray[waypointSortByDistance[wayIndex].index].dist;
        }

        let waypointinfo = getWaypointToHtml(waypointSortByDistance);
        $('#waypointinfoViewTable').html(waypointinfo);

        makeSlope();
        makeChartIcon();

        drawPlot();
        let gridMarkings = plot.getOptions().grid.markings;
/*        _markings.forEach(function (mark) {
            //아이콘의 세로선은 필요하지 않을 듯....
            gridMarkings.push({color: '#f00', lineWidth: 1, xaxis: {from: mark.x, to: mark.x}, yaxis: {from: 0, to: mark.y}});
        });*/

        //경사도에 따른 색상을 표시
        _eleArray.forEach(function (mark) {
            let backgroundColor;
            if(Math.abs(mark[2]) >= 30)
                backgroundColor = "#FF0000";
            else if(Math.abs(mark[2]) >= 20 && Math.abs(mark[2]) < 30)
                backgroundColor = "#21ECFF";
            else {
                mark[1] = 0;
                //backgroundColor = "#ffffff";
            }

            gridMarkings.push({color: backgroundColor, lineWidth: 1,
                    xaxis: {from: mark[0], to: mark[0]},
                    yaxis: {from: 0, to: mark[1]}});
        });

        plot.setupGrid(); // 그리드 업데이트
        plot.draw(); // 차트 다시 그리기

        _eleArray = []; //차트정보 초기화
    }

    $('#saveas').click(function (e) {
        if ($('#gpx_metadata_name').val() === '') {
            alert('경로명이 없습니다.');
            return;
        }
        if (_gpxTrkseqArray.length === 0) {
            alert('경로 정보가 없습니다.');
            return;
        }

        _filetype = $(':radio[name="filetype"]:checked').val();
        //파일명이 없으면
        if (_uploadFilename === '')
            _uploadFilename = $('#gpx_metadata_name').val();

        if ($('#gpx_metadata_name').val() === '') {
            alert('경로명으로 파일을 저장합니다. 경로명을 입력하세요');
            return;
        }

        $('#blockingAds').show();

        //저장할때 포인트를 생성한다.
        getWaypointInfo();

        let saveData;
        if (_filetype === 'gpx') {
            saveData = saveGpx(_uploadFilename, Number($('#averageV').val()),
                 waypointSortByDistance, _gpxTrkseqArray);
        } else {
            saveData = saveTcx(_uploadFilename, Number($('#averageV').val()),
                waypointSortByDistance, _gpxTrkseqArray);
        }

        saveAs(new Blob([saveData], {
            type: "application/vnd.garmin.tcx+xml"
        }), $('#gpx_metadata_name').val() + '.' + _filetype);

        $('#blockingAds').hide();
    });
});

function makeSlope() {
    for (let i = 0; i < _gpxTrkseqArray.length; i++) {
        //좌우 2개값을 기준으로 기울기
        if (i > 2 && i < _gpxTrkseqArray.length - 2) {
            let leftDistance = (_gpxTrkseqArray[i - 2].dist - _gpxTrkseqArray[i - 1].dist) / 2;
            let rightDistance = (_gpxTrkseqArray[i + 1].dist - _gpxTrkseqArray[i + 2].dist) / 2;
            //왼쪽 2개의 중앙에서 오른쪽 중앙의 거리
            let distance = (Math.abs(leftDistance) + Math.abs(rightDistance) +
                Math.abs(_gpxTrkseqArray[i + 1].dist - _gpxTrkseqArray[i - 1].dist)) * 1000;

            let leftElevation = (_gpxTrkseqArray[i - 2].ele + _gpxTrkseqArray[i - 1].ele) / 2;
            let rightElevation = (_gpxTrkseqArray[i + 1].ele + _gpxTrkseqArray[i + 2].ele) / 2;
            let elevationChange = rightElevation - leftElevation;

            let slope = calculateSlope(distance, elevationChange);
            //console.log('slope:' + slope + ', elevation:' + elevationChange + ', distance:' + distance);
            //steepPoints.push({x: _gpxTrkseqArray[i].dist, y: _gpxTrkseqArray[i].ele, slope: slope});

            //chart의 x, y축을 위한 데이터
            _eleArray.push([_gpxTrkseqArray[i].dist, Number(_gpxTrkseqArray[i].ele), slope]);
        } else {
            //양쪽 끝점 2개는 기울기를 0으로 처리
            _eleArray.push([_gpxTrkseqArray[i].dist, Number(_gpxTrkseqArray[i].ele), 0]);
        }
    }
}

function makeChartIcon() {
    //chart에 water, summit 아이콘을 표시하기 위한 데이터
    _markings = [];
    for (let j = 0; j < waypointSortByDistance.length; j++) {
        if (waypointSortByDistance[j].symbol === 'water' || waypointSortByDistance[j].symbol === 'summit') {
            for (let k = 0; k < _gpxTrkseqArray.length; k++) {
                if ((waypointSortByDistance[j].point.lat == _gpxTrkseqArray[k].lat) &&
                    (waypointSortByDistance[j].point.lng == _gpxTrkseqArray[k].lng)) {
                    _markings.push({
                        x: _eleArray[k][0],
                        y: _eleArray[k][1],
                        color: '#FF0000',
                        sym: waypointSortByDistance[j].symbol,
                        name: waypointSortByDistance[j].symbolName
                    });
                }
            }
        }
    }
}

function chartPlotAdView(view) {
    /*
        if(view)    //true
            $('.containerPlot').css('background-image', 'url(\'/images/charter-ad.png\')');
        else
            $('.containerPlot').css('background-image', 'none');
    */
}
