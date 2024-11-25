
function viewGpx(uuid) {
    const url = `/manage/giljabi2-admin.html?fileid=${uuid}&version=v2`;
    window.open(url, '_blank');
}

function showMessage(message, bgColor) {
    // 메시지와 배경색 설정
    $('#message').text(message) // 메시지 설정
        .css({
            'background-color': bgColor,
            'color': '#fff',
            'padding': '10px',
            'border-radius': '5px',
            'text-align': 'center',
            'margin': '10px 0',
            'font-size': '14px'
        })
        .fadeIn(); // 메시지 표시

    // 3초 뒤 사라짐
    setTimeout(function() {
        $('#message').fadeOut(); // 서서히 사라짐
    }, 1000);
}

function showModallessConfirm(message, onConfirm, onCancel) {
    $('#modalless-message').text(message);

    $('#modalless-confirm').fadeIn();

    $('#modalless-true').off('click').on('click', function () {
        $('#modalless-confirm').fadeOut();
        if (onConfirm) onConfirm();
    });

    $('#modalless-false').off('click').on('click', function () {
        $('#modalless-confirm').fadeOut();
        if (onCancel) onCancel();
    });

    $('#modalless-cancel').off('click').on('click', function () {
        $('#modalless-confirm').fadeOut();
    });
}


function changeShareFlag(trackname, uuid) {
    showModallessConfirm(trackname +"을 공유 및 장기보관으로 변경할까요?",
        function() {
            changeShareFlagRequest(uuid, true);
        },
        function() {
            changeShareFlagRequest(uuid, false);
        }
    );
}

function changeShareFlagRequest(uuid, shareflag) {
    $.ajax({
        url: `/api/1.0/changeGpx/${uuid}/${shareflag}`,
        type: 'PATCH',
        contentType: 'application/json',
        success: function(response) {
            if(response.status === 0) {
                showMessage("Gpx 상태가 성공적으로 변경되었습니다.", '#28a745');
                $('#searchButton').trigger('click');
            } else
                alert(response.message);
        },
        error: function(xhr, status, error) {
            console.error("Error:", error); // 오류 응답 처리
            alert("업데이트 중 오류가 발생했습니다.");
        }
    });
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
                            <tr class="${item.shareflag ? 'bg-success-subtle' : ''}">
                                <td class="text-center d-none d-md-table-cell">${item.useruuid}</td>
                                <td class="text-center d-none d-md-table-cell">
                                    ${item.userid ? item.userid.substring(0, item.userid.indexOf('@')) : ''}
                                </td>
                                <td class="text-center d-none d-md-table-cell">${item.createat}</td>
                                <td class="text-end d-none d-md-table-cell">${item.wpt.toLocaleString()}</td>
                                <td class="text-end d-none d-md-table-cell">${item.trkpt.toLocaleString()}</td>
                                <td style="cursor: pointer;" 
                                onclick="javascript:viewGpx('${item.uuid}');">${item.trackname}</td>
                                <td class="text-end">${item.speed}</td>
                                <td class="text-end">${item.distance.toLocaleString()}</td>
                                <td class="text-center">${item.fileext}</td>
                                <td class="text-center"
                                    style="cursor: pointer;" onclick="javascript:changeShareFlag('${item.trackname}', '${item.uuid}');"
                                >${item.shareflag ? 'true' : ''}</td>
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


