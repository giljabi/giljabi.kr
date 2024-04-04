let xmlData;
let NEWLINE = '\n';

function gpxHeader() {
    xmlData = '';	//저장할때 초기화
    xmlData += '<?xml version="1.0" encoding="UTF-8"?>' + NEWLINE;
    if (_filetype === 'gpx') {
        xmlData += '<gpx creator="Giljabi" version="1.1"' + NEWLINE;
        xmlData += '	 xsi:schemaLocation="http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/11.xsd"' + NEWLINE;
        xmlData += '	 xmlns:ns3="http://www.garmin.com/xmlschemas/TrackPointExtension/v1"' + NEWLINE;
        xmlData += '	 xmlns="http://www.topografix.com/GPX/1/1"' + NEWLINE;
        xmlData += '	 xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:ns2="http://www.garmin.com/xmlschemas/GpxExtensions/v3">' + NEWLINE;
    } else {
        xmlData += '<TrainingCenterDatabase xmlns="http://www.garmin.com/xmlschemas/TrainingCenterDatabase/v2"' + NEWLINE;
        xmlData += ' xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"' + NEWLINE;
        xmlData += ' xsi:schemaLocation="http://www.garmin.com/xmlschemas/TrainingCenterDatabase/v2 http://www.garmin.com/xmlschemas/TrainingCenterDatabasev2.xsd">' + NEWLINE;
    }
}

function gpxMetadata(filename, velocity, time) {
    if (_filetype === 'gpx') {
        xmlData += '<metadata>' + NEWLINE;
        xmlData += '  <name>' + filename + '</name>' + NEWLINE;
        xmlData += '  <link href="http://www.giljabi.kr" />' + NEWLINE;
        xmlData += '  <desc>giljabi</desc>' + NEWLINE;
        xmlData += '  <copyright>giljabi.kr</copyright>' + NEWLINE;
        xmlData += '  <speed>' + velocity + '</speed>' + NEWLINE;
        xmlData += '  <time>' + time + '</time>' + NEWLINE;
        xmlData += '</metadata>' + NEWLINE;
    } else {
        xmlData += '<Folders>' + NEWLINE;
        xmlData += '	<Courses>' + NEWLINE;
        xmlData += '		<CourseFolder Name="giljabi.kr">' + NEWLINE;
        xmlData += '			<CourseNameRef>' + NEWLINE;
        xmlData += '				<Id>' + filename + '</Id>' + NEWLINE;
        xmlData += '				<Author>Giljabi</Author>' + NEWLINE;
        xmlData += '			</CourseNameRef>' + NEWLINE;
        xmlData += '		</CourseFolder>' + NEWLINE;
        xmlData += '	</Courses>' + NEWLINE;
        xmlData += '</Folders>' + NEWLINE;

        xmlData += '<Courses>' + NEWLINE;	//여러 코스중에서 하나
        xmlData += '	<Course>' + NEWLINE;	//여러 코스중에서 하나
        xmlData += '		<Speed>' + $('#averageV').val() + '</Speed>' + NEWLINE;
        xmlData += '		<Name>' + $('#gpx_metadata_name').val() + '</Name>' + NEWLINE;

    }
}


/**
 * tcx lap
 * @param firstPoint
 * @param lastPoint
 */
function makeTcxLap(firstPoint, lastPoint) {
    let diff = new Date(lastPoint.time) - new Date(BASETIME);
    xmlData += '		<Lap>' + NEWLINE;
    xmlData += '			<TotalTimeSeconds>' + diff / 1000 + '</TotalTimeSeconds>' + NEWLINE;
    xmlData += '			<DistanceMeters>' + lastPoint.distance + '</DistanceMeters>' + NEWLINE;
    xmlData += '			<BeginPosition>' + NEWLINE;
    xmlData += '				<LatitudeDegrees>' + firstPoint.lat.toFixed(6) + '</LatitudeDegrees>' + NEWLINE;
    xmlData += '				<LongitudeDegrees>' + firstPoint.lng.toFixed(6) + '</LongitudeDegrees>' + NEWLINE;
    xmlData += '			</BeginPosition>' + NEWLINE;
    xmlData += '			<EndPosition>' + NEWLINE;
    xmlData += '				<LatitudeDegrees>' + lastPoint.lat.toFixed(6) + '</LatitudeDegrees>' + NEWLINE;
    xmlData += '				<LongitudeDegrees>' + lastPoint.lng.toFixed(6) + '</LongitudeDegrees>' + NEWLINE;
    xmlData += '			</EndPosition>' + NEWLINE;
    xmlData += '			<Intensity>Active</Intensity>' + NEWLINE;
    xmlData += '		</Lap>' + NEWLINE;
    //xmlData += '		<Track>' + NEWLINE;
}

/**
 * 소수점 자리수는 6자리를 사용, xml 파일 크기를 줄이기 위함
 * 웨이포인트를 계산할때는 시작과 끝을 보여주지만 실제 가민에서는 사용하면 시작과 끝을 추가해서 보여주므로
 * 저잘할때는 제거하고 저장한다.
 * @param waypoint
 */
function gpxWaypoint(waypoint) {
    if (_filetype === 'gpx') {
        for (let i = 0; i < waypoint.length; i++) {
            let wpt = waypoint[i];

            xmlData += '	<wpt lat="' + wpt.point.lat.toFixed(6) +
                '" lon="' + wpt.point.lng.toFixed(6) + '">' + NEWLINE;
            xmlData += '		<name>' + wpt.symbolName + '</name>' + NEWLINE;
            xmlData += '		<sym>' + wpt.symbol + '</sym>' + NEWLINE;
            xmlData += '	</wpt>' + NEWLINE;
        }
    } else {
        for (let i = 0; i < waypoint.length; i++) {
            let wpt = waypoint[i];

            xmlData += '		<CoursePoint>' + NEWLINE;
            xmlData += '			<Name>' + wpt.symbolName + '</Name>' + NEWLINE;
            xmlData += '			<Time>' + wpt.time + '</Time>' + NEWLINE;
            xmlData += '			<Position>';
            xmlData += '				<LatitudeDegrees>' + wpt.point.lat.toFixed(6) + '</LatitudeDegrees>';
            xmlData += '				<LongitudeDegrees>' + wpt.point.lng.toFixed(6) + '</LongitudeDegrees>';
            xmlData += '			</Position>' + NEWLINE;
            xmlData += '			<PointType>' + wpt.symbol.charAt(0).toUpperCase() + wpt.symbol.slice(1) + '</PointType>' + NEWLINE;
            xmlData += '		</CoursePoint>' + NEWLINE;
        }
    }
}

function gpxTrack(trackArray) {
    if (_filetype === 'gpx') {
        let xmlDataParts = [];
        xmlDataParts.push('<trk>');
        xmlDataParts.push(' <trkseg>');
        trackArray.forEach(track => {
            xmlDataParts.push(` <trkpt lat="${track.lat.toFixed(6)}" lon="${track.lng.toFixed(6)}">`);
            xmlDataParts.push(` <ele>${Math.round(track.ele)}</ele>`);
            xmlDataParts.push(` <time>${track.time}</time>`);
            xmlDataParts.push(` <dist>${track.dist}</dist>`);
            xmlDataParts.push(` <desc>${track.dist}</desc>`);
            xmlDataParts.push('	</trkpt>');
        });
        xmlDataParts.push(' </trkseg>');
        xmlDataParts.push('</trk>');
        xmlDataParts.push('</gpx>');
        xmlData += xmlDataParts.join(NEWLINE);
    } else {
        let xmlDataParts = [];
        xmlDataParts.push('<Track>');
        trackArray.forEach(track => {
            xmlDataParts.push('<Trackpoint>');
            xmlDataParts.push('	<Time>' + track.time + '</Time>');
            xmlDataParts.push('	<Position>');
            xmlDataParts.push('	<LatitudeDegrees>' + track.lat.toFixed(6) + '</LatitudeDegrees>');
            xmlDataParts.push('	<LongitudeDegrees>' + track.lng.toFixed(6) + '</LongitudeDegrees>');
            xmlDataParts.push('	</Position>');
            xmlDataParts.push('	<AltitudeMeters>' + Math.round(track.ele) + '</AltitudeMeters>');
            xmlDataParts.push('	<DistanceMeters>' + track.dist + '</DistanceMeters>');
            xmlDataParts.push('</Trackpoint>');
        });
        xmlDataParts.push('	</Track>');
        xmlData += xmlDataParts.join(NEWLINE);
    }
}

function tcxClose() {
    xmlData += '	</Course>' + NEWLINE;
    xmlData += '</Courses>' + NEWLINE;
    xmlData += '</TrainingCenterDatabase>' + NEWLINE;
}
