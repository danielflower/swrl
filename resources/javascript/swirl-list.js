import http from './http.js';

var currentFilter = null;

var respondAndRemove = function (element, response) {
    var swirlElement = $(element)[0].parentNode.parentNode;
    var swirlID = swirlElement.getAttribute('id');
    $(swirlElement).remove();
    http.post('/swirls/' + swirlID + '/respond', {responseButton: response});
};

var filterVisibleSwirls = function ($) {
    $('.type-filter button').each((i, typeButton) => {
        const curType = typeButton.getAttribute('data-swirl-type');
        const show = currentFilter == null || currentFilter === curType;
        $(typeButton).toggleClass('hidden', !show);
        $('.swirl.' + curType).toggle(show);
    });
};
function init($) {

    $(window).scroll(function() {
       if($(window).scrollTop() + $(window).height() + 320 >= $(document).height()) { // 320 is the height of a swrl
           $('.more-swirls-button').click();
       }
    });

    $('.more-swirls-button').click((e) => {
        const b = $(e.target);
        if (b.attr('data-disabled')) {
            return false; // button is disabled as it is still loading data, so do nothing
        }

        // data-ids contains a fixed number of IDs that come AFTER those returned from the server.
        const ids = b.attr('data-ids').split(',');
        const numLoads = parseInt(b.attr('data-num-loads'), 10);
        const pageSize = parseInt(b.attr('data-per-page'), 10);
        const from = numLoads * pageSize;
        const to = from + pageSize;
        const idsToGet = ids.slice(from, to);
        const startIndex = parseInt(b.attr('data-start-index'), 10);
        const nextPageUrl = b.attr('data-url-prefix') + (startIndex + to + pageSize);
        const nextPageUrlIfQueryFails = b.attr('data-url-prefix') + (startIndex + to);
        const originalValue = b.text();

        if (idsToGet.length === 0) {
            return true; // run out of pre-fetched IDs so let the standard link work
        }

        b.attr('data-disabled', 'true'); // disable the button
        b.html('<i class="fa fa-spin fa-spinner"></i> Loading');

        const fetchUrl = '/api/v1/swirls?swirl-list=' + idsToGet.join(',');
        fetch(fetchUrl, {credentials: 'same-origin'})
            .then((resp) => {
                if (resp.status !== 200) {
                    throw 'from ' + fetchUrl + ': ' + resp.status + ' ' + resp.statusText;
                }
                return resp.text();
            })
            .then((html) => {
                $('.swirl-insertion-point').before(html);
                filterVisibleSwirls($);
                setTimeout(function () { $('.pending-to-appear').css('opacity', '1.0') }, 20);
                b.removeAttr('data-disabled');
                b.text(originalValue);
                b.attr('href', nextPageUrl);
            })
            .catch((e) => {
                console.log('Error while getting more', e);
                location.href = nextPageUrlIfQueryFails;
            });

        b.attr('data-num-loads', numLoads + 1);
        return false;
    });

    $('.type-filter button').click((b) => {
        const clickedType = b.target.getAttribute('data-swirl-type');
        currentFilter = (clickedType === currentFilter) ? null : clickedType;
        filterVisibleSwirls($);
    });

    var swirlAndRemove = function (element) {
        var swirlElement = $(element)[0].parentNode.parentNode;
        var swirlTitle = swirlElement.getAttribute('data-title');
        var swirlImageUrl = swirlElement.getAttribute('data-image-url');
        var swirlReview = swirlElement.getAttribute('data-review');
        var swirlType = swirlElement.getAttribute('data-swirl-type');
        $(swirlElement).remove();
        http.post('/swirls/create-swirl', {title: swirlTitle,
                                           review: swirlReview,
                                           type: swirlType,
                                           imageUrl: swirlImageUrl});
    };
    $('.swirl-list').on('click', 'i.add-to-wishlist-button', function () {
        swirlAndRemove(this);
    });

    $('.swirl-list').on('click', 'i.dismiss-button', function () {
        respondAndRemove(this, 'Dismissed');
    });

    $('.swirl-list').on('click', 'i.later-button', function () {
        respondAndRemove(this, 'Later');
    });
}


module.exports = {
    init: init
};