var post = function (url, json) {
    return fetch('/api/v1' + url, {
        method: 'post',
        credentials: 'same-origin',
        headers: {
            'Accept': 'application/json',
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(json)
    });
};
module.exports = { post: post };