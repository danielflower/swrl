function timeSince(date) {

    var seconds = Math.floor((new Date() - date) / 1000);

    var interval = Math.floor(seconds / 2592000);
    if (interval > 1) {
        return date.toDateString();
    }
    interval = Math.floor(seconds / 86400);
    if (interval > 1) {
        return interval + " days ago";
    }
    interval = Math.floor(seconds / 3600);
    if (interval > 1) {
        return interval + " hours ago";
    }
    interval = Math.floor(seconds / 60);
    if (interval > 1) {
        return interval + " minutes ago";
    }
    return Math.floor(seconds) + " seconds ago";
}

function init($) {
    $('time').each((i, el) => {
        var $el = $(el);
        var date = new Date($el.attr('datetime'));
        $el.html(timeSince(date));
    });
}

module.exports = { init: init };