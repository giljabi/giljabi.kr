
function viewGpx(uuid) {
    const url = `/v2/giljabi2.html?fileid=${uuid}&version=v2`;
    window.open(url, '_blank');
}


$(document).ready(function () {
    // 기본 페이징 변수
    let page = 0;
    const size = 20;

    // AJAX 요청
    function loadGpsData() {
        $.ajax({
            url: '/api/1.0/getGpxList',
            method: 'GET',
            data: {
                trackName: $('#trackName').val(),
                useruuid: saveUUID(),
                selfCheck: $('#selfCheck').is(':checked'),
                page: page,
                size: size
            },
            success: function (response) {
                if(response.status === 0) {
                    let rows = '';
                    response.data.content.forEach(item => {
                        rows += `
                            <tr style="cursor: pointer;" onclick="javascript:viewGpx('${item.uuid}');">
                                <td class="text-center d-none d-md-table-cell">${item.useruuid}</td>
                                <td class="text-center d-none d-md-table-cell">
                                    ${item.userid ? item.userid.substring(0, item.userid.indexOf('@')) : ''}
                                </td>
                                <td class="text-center d-none d-md-table-cell">${item.createat}</td>
                                <td class="text-end d-none d-md-table-cell">${item.wpt.toLocaleString()}</td>
                                <td class="text-end d-none d-md-table-cell">${item.trkpt.toLocaleString()}</td>
                                <td>${item.trackname}</td>
                                <td class="text-end">${item.speed}</td>
                                <td class="text-end">${item.distance.toLocaleString()}</td>
                                <td class="text-center">${item.fileext}</td>
                            </tr>
                        `;
                    });
                    $('#gpsdata-table-body').html(rows);

                    // 페이징 상태 표시 (예: 총 페이지, 현재 페이지)
                    $('#pagination-info').text(`Page ${response.data.number + 1} of ${response.data.totalPages}`);
                } else {
                    alert('Error fetching data:', response.message)
                }
            },
            error: function (error) {
                console.error('Error fetching data:', error);
            }
        });
    }

    $('#searchButton').click(function () {
        const trackName = $('#trackNameInput').val();
        page = 0; // 새로운 검색 시 페이지를 초기화
        loadGpsData(trackName);
    });

    // 다음 페이지 로드
    $('#next-page').click(function () {
        page++;
        loadGpsData();
    });

    // 이전 페이지 로드
    $('#prev-page').click(function () {
        if (page > 0) {
            page--;
            loadGpsData();
        }
    });

    // 초기 데이터 로드
    loadGpsData();
});

