let NEWLINE = '\n';

function saveGpx(filename, velocity, waypoint, trackArray) {
    let xmlDataParts = [];
    xmlDataParts.push('<?xml version="1.0" encoding="UTF-8"?>');
    xmlDataParts.push('<gpx creator="giljabi" version="1.1"');
    xmlDataParts.push(' xsi:schemaLocation="http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/11.xsd"');
    xmlDataParts.push(' xmlns:ns3="http://www.garmin.com/xmlschemas/TrackPointExtension/v1"');
    xmlDataParts.push(' xmlns="http://www.topografix.com/GPX/1/1"');
    xmlDataParts.push(' xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" ' +
        'xmlns:ns2="http://www.garmin.com/xmlschemas/GpxExtensions/v3">');

    xmlDataParts.push(`<metadata>`);
    xmlDataParts.push(` <name>${filename}</name>`);
    xmlDataParts.push(` <link href="http://www.giljabi.kr" />`);
    xmlDataParts.push(` <desc>giljabi</desc>`);
    xmlDataParts.push(` <copyright>giljabi.kr</copyright>`);
    xmlDataParts.push(` <speed>${velocity}</speed>`);
    xmlDataParts.push(` <time>${trackArray[0].time}</time>`);
    xmlDataParts.push(`</metadata>`);

    waypoint.forEach(wpt => {
        xmlDataParts.push(`<wpt lat="${wpt.point.lat.toFixed(6)}" lon="${wpt.point.lng.toFixed(6)}">`);
        xmlDataParts.push(` <name>${wpt.symbolName}</name>`);
        xmlDataParts.push(` <sym>${wpt.symbol}</sym>`);
        xmlDataParts.push(`</wpt>`);
    });

    xmlDataParts.push(`<trk>`);
    xmlDataParts.push(` <trkseg>`);
    trackArray.forEach(track => {
        xmlDataParts.push(` <trkpt lat="${track.lat.toFixed(6)}" lon="${track.lng.toFixed(6)}">`);
        xmlDataParts.push(` <ele>${Math.round(track.ele)}</ele>`);
        xmlDataParts.push(` <time>${track.time}</time>`);
        xmlDataParts.push(` <dist>${track.dist}</dist>`);   //속성이 없음, garmin 기기에서 사용해도 되는지 확인해야 함
        xmlDataParts.push(` <desc>${track.dist}</desc>`);
        xmlDataParts.push(` </trkpt>`);
    });
    xmlDataParts.push(` </trkseg>`);
    xmlDataParts.push(`</trk>`);
    xmlDataParts.push(`</gpx>`);
    return xmlDataParts.join(NEWLINE);
}

function saveTcx(filename, velocity, waypoint, trackArray) {
    let xmlDataParts = [];
    xmlDataParts.push('<?xml version="1.0" encoding="UTF-8"?>');
    xmlDataParts.push('<TrainingCenterDatabase xmlns="http://www.garmin.com/xmlschemas/TrainingCenterDatabase/v2"');
    xmlDataParts.push(' xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"');
    xmlDataParts.push(' xsi:schemaLocation="http://www.garmin.com/xmlschemas/TrainingCenterDatabase/v2 ' +
        'http://www.garmin.com/xmlschemas/TrainingCenterDatabasev2.xsd">');

    xmlDataParts.push(`<Folders>`);
    xmlDataParts.push(` <Courses>`);
    xmlDataParts.push(`  <CourseFolder>`);
    xmlDataParts.push(`  <CourseNameRef><Id>${filename}</Id><Author>Giljabi</Author></CourseNameRef>`);
    xmlDataParts.push(`  </CourseFolder>`);
    xmlDataParts.push(` </Courses>`);
    xmlDataParts.push(`</Folders>`);

    xmlDataParts.push(`<Courses>`);
    xmlDataParts.push(` <Course>`);
    xmlDataParts.push(` <Speed>${velocity}</Speed>`);
    xmlDataParts.push(` <Name>${filename}</Name>`);
    let lastPoint = trackArray[trackArray.length - 1];
    let firstPoint = trackArray[0];
    let diff = new Date(lastPoint.time) - new Date(firstPoint.time);
    xmlDataParts.push(` <Lap>`);
    xmlDataParts.push(` <TotalTimeSeconds>${diff / 1000}</TotalTimeSeconds>`);
    xmlDataParts.push(` <DistanceMeters>${lastPoint.dist}</DistanceMeters>`);
    xmlDataParts.push(` <BeginPosition>`);
    xmlDataParts.push(` <LatitudeDegrees>${firstPoint.lat.toFixed(6)}</LatitudeDegrees>`);
    xmlDataParts.push(` <LongitudeDegrees>${firstPoint.lng.toFixed(6)}</LongitudeDegrees>`);
    xmlDataParts.push(` </BeginPosition>`);
    xmlDataParts.push(` <EndPosition>`);
    xmlDataParts.push(` <LatitudeDegrees>${lastPoint.lat.toFixed(6)}</LatitudeDegrees>`);
    xmlDataParts.push(` <LongitudeDegrees>${lastPoint.lng.toFixed(6)}</LongitudeDegrees>`);
    xmlDataParts.push(` </EndPosition>`);
    xmlDataParts.push(` <Intensity>Active</Intensity>`);
    xmlDataParts.push(` </Lap>`);

    xmlDataParts.push('<Track>');
    trackArray.forEach(track => {
        xmlDataParts.push(`<Trackpoint>`);
        xmlDataParts.push(`	<Time>${track.time}</Time>`);
        xmlDataParts.push(`	<Position>`);
        xmlDataParts.push(`	<LatitudeDegrees>${track.lat.toFixed(6)}</LatitudeDegrees>`);
        xmlDataParts.push(`	<LongitudeDegrees>${track.lng.toFixed(6)}</LongitudeDegrees>`);
        xmlDataParts.push(`	</Position>`);
        xmlDataParts.push(`	<AltitudeMeters>${Math.round(track.ele)}</AltitudeMeters>`);
        xmlDataParts.push(`	<DistanceMeters>${track.dist * 1000}</DistanceMeters>`); //Meter 단위로 변환
        xmlDataParts.push(`</Trackpoint>`);
    });
    xmlDataParts.push('	</Track>');

    waypoint.forEach(wpt => {
        xmlDataParts.push('<CoursePoint>');
        xmlDataParts.push(` <Name>${wpt.symbolName}</Name>`);
        xmlDataParts.push(` <Time>${wpt.time}</Time>`);
        xmlDataParts.push('	<Position>');
        xmlDataParts.push(` <LatitudeDegrees>${wpt.point.lat.toFixed(6)}</LatitudeDegrees>`);
        xmlDataParts.push(` <LongitudeDegrees>${wpt.point.lng.toFixed(6)}</LongitudeDegrees>`);
        xmlDataParts.push('	</Position>');
        xmlDataParts.push(` <PointType>${wpt.symbol.charAt(0).toUpperCase() + wpt.symbol.slice(1)}</PointType>`);
        xmlDataParts.push('</CoursePoint>');
    });
    xmlDataParts.push(' </Course>');
    xmlDataParts.push('</Courses>');
    xmlDataParts.push('</TrainingCenterDatabase>');
    return xmlDataParts.join(NEWLINE);
}