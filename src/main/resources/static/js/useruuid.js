// UUID 생성 함수
function generateUUID() {
    return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
        const r = (Math.random() * 16) | 0;
        const v = c === 'x' ? r : (r & 0x3) | 0x8;
        return v.toString(16);
    });
}

// UUID 저장
function getUUID() {
    let uuid = localStorage.getItem('GILJABI_UUID'); // 기존 UUID 조회
    if (!uuid) {
        localStorage.setItem('GILJABI_UUID', generateUUID()); // UUID 저장
    }
    return uuid;
}



