import http from './http.js';

var currentFilter = null;





var respondAndRemove = function (element, response) {
    var swirlElement = $(element)[0].parentNode.parentNode;
    var swirlID = swirlElement.getAttribute('id');
    var wasAdded = $(swirlElement).hasClass('added');
    $(swirlElement).remove();
    var nextSwirlOption = document.querySelectorAll('#more-swirls option')[0];
    if (nextSwirlOption != null) {
        var nextSwirlID = nextSwirlOption.innerText;
        var nextSwirlHTML = nextSwirlOption.getAttribute('data-value');
        var swirlList = document.getElementById('swirl-list');
        $(swirlList).append(nextSwirlHTML);
        if (wasAdded) {
            // then the Swirl it is replacing was added and we should mark the new Swirl as 'added' too
            $(document.getElementById(nextSwirlID)).addClass('added'); //FIXME: This isn't working and I don't know why
        }
        $(nextSwirlOption).remove();
    }
    http.post('/swirls/' + swirlID + '/respond', {responseButton: response});
};

var filterVisibleSwirls = function ($) {
    $('.type-filter button').each((i, typeButton) => {
        const curType = typeButton.getAttribute('data-swirl-type');
        const show = currentFilter == null || currentFilter === curType;
        $(typeButton).toggleClass('hidden', !show);
        $('.mini-swirl.' + curType).toggle(show);
    });
};
function init($) {

    $('.more-swirls-button').click((e) => {
        const b = $(e.target);
        if (b.attr('data-disabled')) {
            return false; // button is disabled as it is still loading data, so do nothing
        }

        const ids = b.attr('data-ids').split(',');
        const numLoads = parseInt(b.attr('data-num-loads'), 10);
        const pageSize = parseInt(b.attr('data-per-page'), 10);
        const to = numLoads * pageSize + pageSize;
        const idsToGet = ids.slice(numLoads * pageSize, to);
        const nextPageUrl = b.attr('data-url-prefix') + to;
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
                location.href = nextPageUrl;
            });

        b.attr('data-num-loads', numLoads + 1);
        return false;
    });

    $('.type-filter button').click((b) => {
        const clickedType = b.target.getAttribute('data-swirl-type');
        currentFilter = (clickedType === currentFilter) ? null : clickedType;
        filterVisibleSwirls($);
    });

    $('#swirl-list').on('click', 'i.dismiss-button', function () {
        respondAndRemove(this, 'Dismissed');
    });

    $('#swirl-list').on('click', 'i.later-button', function () {
        respondAndRemove(this, 'Later');
    });
}


module.exports = {
    init: init
};