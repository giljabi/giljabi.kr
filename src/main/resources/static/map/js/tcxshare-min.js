var _newPosition,_map,_gpxname,_mapTypes={terrain:daum.maps.MapTypeId.TERRAIN,bicycle:daum.maps.MapTypeId.BICYCLE},_gpx=new Object,_gpxMetadata=new Object,_gpxWptArray=new Array,_gpxTrkArray=new Array,_gpxTrkseqArray=new Array,_tcx=new Object,_tcxMetadata=new Object,_tcxCourses=new Object,_tcxWptArray=new Array,_tcxTrkseqArray=new Array;$(document).ready(function(){var e=document.getElementById("map"),a={center:new daum.maps.LatLng(37.56683546665817,126.9786607449023),level:8};_map=new daum.maps.Map(e,a);var t=new daum.maps.MapTypeControl;function d(e){var a=e.sym,t=e.name,r=document.createElement("div");r.innerHTML='<img src="/images/'+a.toLowerCase()+'.png" class="pointImage"><span class="pointText">'+t+"</span>";new daum.maps.CustomOverlay({map:_map,clickable:!1,content:r,position:e.position})}function r(e){var a,t,r;a=$.parseXML(e),a=$(a),"gpx"==_ft?(_gpx=a.find("gpx"),_gpxMetadata.author=a.find("gpx").find("metadata").find("author").text(),_gpxMetadata.name=a.find("gpx").find("metadata").find("name").text(),_gpxMetadata.desc=a.find("gpx").find("metadata").find("desc").text(),function(e){$.each(e.find("gpx").find("wpt"),function(){var e,a,t,r,n,i,s,p,l,o=(e=$(this).attr("lat"),a=$(this).attr("lon"),t=$(this).find("ele").text(),r=$(this).find("name").text(),n=$(this).find("desc").text(),i=$(this).find("type").text(),l=$(this).find("sym").text(),s=0<="danger,flag,food,generic,left,right,sprint,summit,water,waypoint".indexOf(l)?l:"flag",(p=new Object).uid=_u++,p.position=new daum.maps.LatLng(e,a),p.ele=t,p.name=r,p.desc=n,p.type=i,p.sym=s,p);d(o)});new Number(0),new Number(0);var s=0;$.each(e.find("gpx").find("trk").find("trkseg").find("trkpt"),function(){var e,a,t,r,n=(e=$(this).attr("lat"),a=$(this).attr("lon"),t=$(this).find("ele").text(),(r=new Object).lat=e,r.lon=a,r.ele=t,r);if(_gpxTrkseqArray.push(n),_trkPoly.push(new daum.maps.LatLng($(this).attr("lat"),$(this).attr("lon"))),0<s){var i=_gpxTrkseqArray[s].ele-_gpxTrkseqArray[s-1].ele;0<=i?i:i}else _gpxTrkseqArray[0].ele;s++})}(a)):"tcx"==_ft&&(t=a,$.each(t.find("CoursePoint"),function(){var e=new Object;e.uid=_u++,e.position=new daum.maps.LatLng($(this).find("LatitudeDegrees").text(),$(this).find("LongitudeDegrees").text()),e.ele="",e.name=$(this).find("Name").text(),e.desc="",e.type="",e.sym=$(this).find("PointType").text(),d(e)}),$.each(t.find("Trackpoint"),function(){var e=new Object;e.lat=$(this).find("LatitudeDegrees").text(),e.lon=$(this).find("LongitudeDegrees").text(),e.ele=$(this).find("AltitudeMeters").text(),_gpxTrkseqArray.push(e),_trkPoly.push(new daum.maps.LatLng(e.lat,e.lon))})),makeMarkerPoint(_map,"start",_gpxTrkseqArray[0]),makeMarkerPoint(_map,"end",_gpxTrkseqArray[_gpxTrkseqArray.length-1]),r=_trkPoly,new daum.maps.Polyline({path:r,strokeWeight:5,strokeColor:"#FF0000",strokeOpacity:.7,strokeStyle:"solid"}).setMap(_map);var n=_gpxTrkseqArray[parseInt(_gpxTrkseqArray.length/2)];_map.setCenter(new daum.maps.LatLng(n.lat,n.lon)),_map.setLevel(10),function(){$("#elevationImage .legendLabel");for(var e,a,t,r,l=null,o=null,d=new daum.maps.Marker,n=Array(),i=0,s=Number(0),p=!0,m=(new Date,new Date),c=0,g=0,u=0,f=curAlti=0;f<_gpxTrkseqArray.length;f++)curAlti=Number(_gpxTrkseqArray[f].ele),1==p?(new Date("2018-01-01T00:00:00Z"),m=new Date("2018-01-01T00:00:00Z"),g=curAlti,u=curAlti,e=_gpxTrkseqArray[f].lat,a=_gpxTrkseqArray[f].lon):m=appendTime(c),t=_gpxTrkseqArray[f].lat,r=_gpxTrkseqArray[f].lon,curAlti>=u&&(u=curAlti),curAlti<=g&&(g=curAlti),i=getDistanceFromLatLon(e,a,t,r),s+=Number(i),c=i/3.6,n.push([s/1e3,Number(_gpxTrkseqArray[f].ele)]),0==p&&(m,e=t,a=r),p=!1;function x(){l=null;var e,a,t=o,r=plot.getData();for(e=0;e<r.length;++e){var n=r[e];for(a=0;a<n.data.length&&!(n.data[a][0]>t.x);++a);d.setMap(null),(d=new daum.maps.Marker({position:new daum.maps.LatLng(_gpxTrkseqArray[a].lat,_gpxTrkseqArray[a].lon)})).setMap(_map);var i=getDistanceFromLatLon(_gpxTrkseqArray[a].lat,_gpxTrkseqArray[a].lon,_gpxTrkseqArray[a-1].lat,_gpxTrkseqArray[a-1].lon),s=Number(_gpxTrkseqArray[a].ele)-Number(_gpxTrkseqArray[a-1].ele),p=100*s/i;$("#slope").val(p.toFixed(2)+"%")}}plot=$.plot("#elevationImage",[{data:n}],{crosshair:{mode:"x"},grid:{hoverable:!0,autoHighlight:!1,show:!0,aboveData:!0},yaxis:{min:.7*g,max:1.2*u}}),$("#elevationImage").bind("plothover",function(e,a,t){o=a,l||(l=setTimeout(x,50))})}()}_map.addControl(t,daum.maps.ControlPosition.TOPRIGHT),$(".mapType").change(function(){for(var e in _mapTypes)_map.removeOverlayMapTypeId(_mapTypes[e]);$("#chkTerrain").is(":checked")&&_map.addOverlayMapTypeId(_mapTypes.terrain),$("#chkBicycle").is(":checked")&&_map.addOverlayMapTypeId(_mapTypes.bicycle)}),function(){getParam("fileid",window.location.href);$.ajax({type:"post",url:"/tcxshare.do",data:{fileId:getParam("fileid",window.location.href)},dataType:"json",async:!1,complete:function(){},success:function(e,a){"success"==e.resultcode?(_ft="tcx",$("#gpxname").val(e.gpxname),r(e.tcxdata)):alert(e.resultMessage)}})}(),$("#fileInput").change(function(){if(_fl)alert("이미 기본 파일이 열려 있습니다.");else{var e=document.getElementById("fileInput").files[0];_uf=e.name.substring(0,e.name.lastIndexOf(".")),_ft=e.name.substring(e.name.lastIndexOf(".")+1),_fl=!0;var a=new FileReader;a.onload=function(e){r(a.result)},a.readAsText(e),$("#gpx_metadata_name").val(_uf)}}),$("#filedownload").click(function(){return window.location.href="/download?fileId="+getParam("fileid",window.location.href),!1})});