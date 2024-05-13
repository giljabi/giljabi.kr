function deg2rad(deg) {
    return deg * (Math.PI / 180);
}

function getDistance(fromPoint, toPoint) {
    let R = 6371e3;
    let dLat = deg2rad(toPoint.lat - fromPoint.lat);
    let dLon = deg2rad(toPoint.lng - fromPoint.lng);
    let a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
        Math.cos(deg2rad(fromPoint.lat)) * Math.cos(deg2rad(toPoint.lat)) *
        Math.sin(dLon / 2) * Math.sin(dLon / 2);
    let c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    return R * c;// * 0.922125;    //가민거리와 비교해서 보정
}

/**
 * 가변으로 생성되는 input box의 id값
 * @param get_as_float
 * @returns
 */
function microtime(get_as_float) {
    // Returns either a string or a float containing the current time in seconds and microseconds
    // version: 812.316 
    // discuss at: http://phpjs.org/functions/microtime 
    // +   original by: Paulo Ricardo F. Santos 
    // *     example 1: timeStamp = microtime(true); 
    // *     results 1: timeStamp > 1000000000 && timeStamp < 2000000000 
    let now = new Date().getTime() / 1000;
    let s = parseInt(now);
    return (get_as_float) ? now : (Math.round((now - s) * 1000) / 1000) + ' ' + s;
}

/**
 * 기준시간에서 평속으로 가는 시간, 가상라이더
 */
function appendTime(time) {
    let defaultTime = new Date('2018-01-01T00:00:00Z');
    defaultTime.setSeconds(defaultTime.getSeconds() + time);
    return defaultTime.toISOString().split('.', 1) + 'Z';
}

let _microTime = Math.round(microtime(true) * 100);
let _fileExt;	//_ft 파일종류 gpx, tcx

//시작, 도착 마커
function makeMarkerPoint(mymap, iconName, latlon) {
    let marker = new kakao.maps.Marker({
        position: new kakao.maps.LatLng(latlon.lat, latlon.lng),
        image: new kakao.maps.MarkerImage(
            'images/' + iconName + '.png',
            new kakao.maps.Size(17, 22))
    });
    marker.setMap(mymap);
}

function xmlToString(xmlData) {
    var xmlString;
    //IE
    if (window.ActiveXObject) {
        xmlString = xmlData.xml;
    }
    // code for Mozilla, Firefox, Opera, etc.
    else {
        xmlString = (new XMLSerializer()).serializeToString(xmlData);
    }
    return xmlString;
}

/**
 * return waypoint
 * @param lat
 * @param lon
 * @param ele
 * @param name
 * @param desc
 * @param type
 * @param sym
 * @returns {{}}
 * getGpxWpt
 */
function GpxWaypoint(lat, lng, ele, name, desc, type, sym) {
    this.uid = _microTime++;
    this.position = new kakao.maps.LatLng(Number(lat.toFixed(6)), Number(lng.toFixed(6)));
    this.ele = isNaN(ele) ? 0 : Number(ele.toFixed(2));
    this.name = name;		//웨이포인트 이름
    this.desc = desc;		//웨이포인트 설명
    this.type = type;		//sym 설명
    this.sym = sym;
}

GpxWaypoint.prototype.toString = function toString() {
    return JSON.stringify($(this)[0], null, 2);
}

/**
 * 입력값이 문자일수도 있어 Number로 변환
 * @param lat
 * @param lon
 * @param ele
 * @returns {*
 * getGpxTrk
 * 좌표정보는 소수점 이하 6자리만 사용
 */
function Point3D(lat, lng, ele, dist, time, hr, atemp) {
    this.lat = Number(lat.toFixed(6));
    this.lng = Number(lng.toFixed(6));
    this.ele = isNaN(ele) ? 0 : Number(ele.toFixed(2));
    this.dist = isNaN(dist) ? 0 : Number(dist.toFixed(2));  //garmin gpx 포맷에는 없음
    this.time = time;
    this.hr = hr;       //Heart Rate, plot에서만 사용
    this.atemp = atemp; //Air Temperature, plot에서만 사용
}

Point3D.prototype.toString = function toString() {
    return JSON.stringify($(this)[0], null, 2, 0, null);
}

function getIconString(sym) {
    if (_waypointIcons.indexOf(sym) >= 0)
        return sym;
    else
        return 'generic';
}

function WayPointInfo(index, distance, time, point, symbol, symbolName, laptime) {
    this.index = index;
    this.distance = distance;
    this.time = time;
    this.point = point;
    this.symbol = symbol;
    this.symbolName = symbolName;
    this.laptime = laptime;
}

WayPointInfo.prototype.toString = function toString() {
    return JSON.stringify($(this)[0], null, 2);
}


/**
 *
 * @param waypointSortByDistance
 * @returns {string}
 */
function getWaypointToHtml(waypointSortByDistance) {
    //우측에 웨이포인트를 출력하고, 엑셀로 저장할때도 사용한다.
    let waypointInfo = '<table "style=border:0px;border-collapse: collapse;">';
    waypointSortByDistance.forEach(waypoint => {
        const sym = waypoint.symbol.toLowerCase();
        const dist = (waypoint.distance / 1000).toFixed(2);
        waypointInfo += `
            <tr onclick="javascript:goCenter(${waypoint.point.lat}, ${waypoint.point.lng}, 5);">
                <td><img src="/images/${sym}.png" width="15px" height="18px"></td>
                <td style="width=110px;" class="timeClass">${waypoint.symbolName}</td>
                <td style="width=20px;align=right" class="timeClass">${dist}</td>
                <td style="width=70px;align=right" class="timeClass">${waypoint.laptime}</td>
            </tr>`;
    });
    waypointInfo += '</table>';
    return waypointInfo;
}

/**
 * excel 저장을 위한 데이터
 * @param waypointSortByDistance
 * @returns {[]}
 */
function getWaypointToExcel(waypointSortByDistance) {
    let waypointinfo = [];
    waypointinfo.push(['번호', '기호', '웨이포인트', '거리(M)', '통과시간'])
    for (let i = 0; i < waypointSortByDistance.length; i++) {
        waypointinfo.push([
            (i + 1),
            waypointSortByDistance[i].symbol,
            waypointSortByDistance[i].symbolName,
            waypointSortByDistance[i].distance,
            waypointSortByDistance[i].time]);
    }
    return waypointinfo;
}

/**
 * 웨이포인트의 정확한 위치를 경로상에서 찾는다
 * START, END는 거리 계산을 위해 임시로 사용하고 최종 데이터 출력할때는 삭제해야 한다
 * @param wpt
 * @returns {this}
 */
function makeWaypointInfo(wpt) {
    let nearPoint;
    let waypointSortList = [];

    for (let indexWpt = 0; indexWpt < wpt.length; indexWpt++) {
        let compareDistance = 0;
        let trackIndex = 0;
        let fromPoint = new Point3D(wpt[indexWpt].lat, wpt[indexWpt].lng, 0);
        //경로상에 있는 포인트들과 각각의 웨이포인트의 거리를 비교하여 가장 가까운 거리에
        //있는 포인트를 웨이포인트의 좌표로 설정하여 웨이포인트의 순서를 정렬한다.
        for (let index = 0; index < _gpxTrkseqArray.length; index++) {
            let toPoint = _gpxTrkseqArray[index];
            let trackDistance = getDistance(fromPoint, toPoint);
            if (index == 0)
                compareDistance = trackDistance;

            //웨이포인트에서 가장 가까이 위치한 포인트
            if (trackDistance <= compareDistance) {
                compareDistance = trackDistance;
                trackIndex = index;
                //console.info('distance:' + trackDistance);
            }
        }
        nearPoint = new WayPointInfo(trackIndex, 0, '0',
            _gpxTrkseqArray[trackIndex], wpt[indexWpt].sym, wpt[indexWpt].name);
        waypointSortList.push(nearPoint);
        //console.info(nearPoint.toString());
    }
    //console.info(waypointSortList.toString());
    //웨이포인트를 index 기준으로 정렬한다.
    let waypointSortByDistance = waypointSortList.sort(function (a, b) {
        return a.index - b.index;
    });

    return waypointSortByDistance;
}

/**
 * 웨이포인트를 엑셆파일로 저장한다.
 * @param filename
 * @param excelData
 */
function excelFileExport(filename, excelData) {
    let sheetName = 'waypoint';
    let wb = XLSX.utils.book_new();
    wb.SheetNames.push(sheetName);
    wb.Sheets[sheetName] = XLSX.utils.aoa_to_sheet(excelData);

    let wbout = XLSX.write(wb, {bookType: 'xlsx', type: 'binary'});

    saveAs(new Blob([s2ab(wbout)], {
        type: "application/octet-stream"
    }), filename + '.xlsx');
}

function s2ab(s) {
    let buf = new ArrayBuffer(s.length); //convert s to arrayBuffer
    let view = new Uint8Array(buf);  //create uint8array as viewer
    for (let i = 0; i < s.length; i++)
        view[i] = s.charCodeAt(i) & 0xFF; //convert to octet
    return buf;
}

//지도의 중심으로 이동, _map global 변수 사용
function goCenter(lat, lng, level) {
    _map.setLevel(5);
    _map.setCenter(new kakao.maps.LatLng(lat, lng));
}

/**
 * tcx 파일을 만들때 사용
 * @param point
 * @param time
 * @param dist
 * @constructor
 */
function TrackPoint(point, time, dist) {
    this.time = time;
    this.position = new Point3D(point.lat, point.lng, point.ele.toFixed(0));
    this.distance = dist.toFixed(2);      //meter
}

TrackPoint.prototype.toString = function toString() {
    return JSON.stringify($(this)[0], null, 2);
}


function calculateSlope(distance, elevationChange) {
    // 경사 = (고도 변화 / 거리) * 100 (퍼센트로 변환)
    return Number(((elevationChange / distance) * 100).toFixed(0));
}

// 주어진 GPX 데이터 배열 (_gpxTrkseqArray)에서 10% 이상의 경사를 가진 포인트 찾기
function findSteepSlopes(gpxTrkseqArray) {
    let steepPoints = [];

    for (let i = 2; i < gpxTrkseqArray.length - 2; i++) {
        // 이전과 다음 포인트로부터 평균 거리와 고도 계산
        let avgDistanceBefore = (gpxTrkseqArray[i].dist - gpxTrkseqArray[i - 2].dist) / 2;
        let avgDistanceAfter = (gpxTrkseqArray[i + 2].dist - gpxTrkseqArray[i].dist) / 2;
        let avgElevationBefore = (gpxTrkseqArray[i].ele - gpxTrkseqArray[i - 2].ele) / 2;
        let avgElevationAfter = (gpxTrkseqArray[i + 2].ele - gpxTrkseqArray[i].ele) / 2;

        // 양쪽 포인트를 기준으로 평균 경사 계산
        let slopeBefore = calculateSlope(avgDistanceBefore, avgElevationBefore);
        let slopeAfter = calculateSlope(avgDistanceAfter, avgElevationAfter);

        // 앞뒤 경사의 평균 계산
        let avgSlope = (slopeBefore + slopeAfter) / 2;

        // 평균 경사가 10% 이상이면 결과 배열에 추가
        if (Math.abs(avgSlope) >= 10) {
            steepPoints.push(gpxTrkseqArray[i]);
        }
    }

    return steepPoints;
}

/**
 * 고도 상승과 하강 계산
 * @param gpxTrkseqArray
 * @returns {{up: number, down: number}}
 */
function analyzePoints(points) {
    let totalRise = 0;
    let totalFall = 0;
    let maxHeartRate = 0;
    let maxHeartPos = 0;
    let highestTemp = -99; // Initialize to very low to find the max
    let lowestTemp = 99; // Initialize to very high to find the min
    let highestTempPos = 0;
    let lowestTempPos = 0;

    for (let i = 1; i < points.length; i++) {
        // Calculate total rise and fall
        let elevationChange = points[i].ele - points[i - 1].ele;
        if (elevationChange > 0) {
            totalRise += elevationChange;
        } else {
            totalFall -= elevationChange; // elevationChange is negative, so we subtract to add the positive value.
        }

        // Find maximum heart rate
        if (points[i].hr > maxHeartRate) {
            maxHeartRate = points[i].hr;
            maxHeartPos = i;
        }

        // Find highest and lowest temperature
        if (points[i].atemp > highestTemp) {
            highestTemp = points[i].atemp;
            highestTempPos = i;
        }
        if (points[i].atemp < lowestTemp) {
            lowestTemp = points[i].atemp;
            lowestTempPos = i;
        }
    }

    return {
        totalRise: Math.ceil(totalRise),
        totalFall: Math.ceil(totalFall),
        maxHeartRate: maxHeartRate,
        maxHeartPos: maxHeartPos,
        highestTemp: highestTemp,
        highestTempPos: highestTempPos,
        lowestTemp: lowestTemp,
        lowestTempPos: lowestTempPos,
    };
}