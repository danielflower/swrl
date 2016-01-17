
var currentSearch = null;
var currentTimeout = null;

window.onpopstate = function(event) {
    const q = ((event.state) ? event.state.query : '') || '';
    $('.search-form .query').val(q);
    $('.search-form').submit();
};

function init($) {

    $('.search-form .query').keydown((e, k) => {
        if (currentTimeout) {
            window.clearTimeout(currentTimeout);
        }
        currentTimeout = window.setTimeout(() => {$('.search-form').submit();}, 300);
    });

    $('.search-form').submit((e) => {
        const $f = $(e.currentTarget);
        const query = $f.find('.query').val();
        currentSearch = query;
        const $summary = $f.find('.search-result-summary');
        $summary.toggleClass('no-query', !query);

        if (!query) {
            return true; // nothing entered... let the browser deal with it
        }
        const $b = $f.find('.submit');

        const $summaryQuery = $f.find('.query-val');
        $summaryQuery.text(query);
        const $resultCount = $f.find('.result-count');
        $resultCount.html('<i class="fa fa-spin fa-spinner"></i>');

        const fetchUrl = '/api/v1/swirls/search?query=' + encodeURIComponent(query);
        fetch(fetchUrl, {credentials: 'same-origin'})
            .then((resp) => {
                if (resp.status !== 200) {
                    throw 'from ' + fetchUrl + ': ' + resp.status + ' ' + resp.statusText;
                }
                return resp.text();
            })
            .then((html) => {
                if (currentSearch === query) {
                    $('.search-results').html(html);
                    $resultCount.html($('.search-results .mini-swirl').length);
                    $b.html('Go');
                    requestAnimationFrame(() => {$('.pending-to-appear').css('opacity', '1.0')});
                    history.replaceState({query: query}, 'Search results for ' + query, '/search?query=' + encodeURIComponent(query));
                }
            })
            .catch((e) => {
                console.log('Error while getting search results', e);
                $f.off();
                $f.submit();
            });


        return false;
    })
}


module.exports = {
    init: init
};