var _manager;
var _drawingManagerOption;
var _globalMap;
var _gpxTrkseqArray = new Array();		//gpx/trk/trkseq
var _trkptPolyline = new Array();		//gpx/trk/trkseq

$(document).ready(function() {
    var options = {
        center: getLocation(), //Seoul city hall
        level: 8
    };
    _globalMap = new kakao.maps.Map(document.getElementById('map'), options);

    var mapTypeControl = new kakao.maps.MapTypeControl(); // 지도타입 컨트롤
    _globalMap.addControl(mapTypeControl, kakao.maps.ControlPosition.TOPRIGHT);

    // 지도 확대 축소를 제어할 수 있는  줌 컨트롤을 생성합니다
    var zoomControl = new kakao.maps.ZoomControl();
    _globalMap.addControl(zoomControl, kakao.maps.ControlPosition.RIGHT);

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
    _manager = new kakao.maps.drawing.DrawingManager(_drawingManagerOption);

    $('#fileInput').change(function() {
        let file = document.getElementById('fileInput').files[0];
        let fileName = file.name.substring(0, file.name.lastIndexOf('.'));
        let fileExt = file.name.substring(file.name.lastIndexOf('.') + 1);
        let reader = new FileReader();

        reader.onload = function(e) {
            makeObject(reader.result);
            console.log(reader.result);	//필요하면 디버깅으로...
        };

        reader.readAsText(file);
    });

});

function makeObject(xml) {
    var x;	//업로드된 파일의 데이터
    x = $.parseXML(xml);
    x = $(x);

    loadGpx(x);

    //시작과 끝 표시
    makeMarkerPoint(_globalMap, 'start', _gpxTrkseqArray[0]);
    makeMarkerPoint(_globalMap, 'end', _gpxTrkseqArray[_gpxTrkseqArray.length - 1]);

    _manager.put(kakao.maps.drawing.OverlayType.POLYLINE, _trkptPolyline);

}

function loadGpx(x) {
    $.each(x.find('gpx > trk > trkseg > trkpt'), function() {
        var trkpt = {
            lat: $(this).attr('lat'),
            lng: $(this).attr('lon'),
            ele: $(this).find('ele').text()
        };
        _gpxTrkseqArray.push(trkpt);
        _trkptPolyline.push(new kakao.maps.LatLng(trkpt.lat, trkpt.lng));
    });
}