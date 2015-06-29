var currentFilter = null;

var showSwirls = function (button) {
    var swirlType = button.getAttribute('data-swirl-type');
    $(button).toggleClass('hidden', false);
    $('.mini-swirl.' + swirlType).show();
};

var hideSwirls = function (button) {
    var swirlType = button.getAttribute('data-swirl-type');
    $(button).toggleClass('hidden', true);
    $('.mini-swirl.' + swirlType).hide();
};

function init($) {
    $('.type-filter button').click((b) => {
        if (currentFilter == null) {
            $('.type-filter button').each((i, typeButton) => {
                if (b.target !== typeButton) {
                    hideSwirls(typeButton);
                }
            });
            currentFilter = b.target;
        } else if (currentFilter === b.target) {
            $('.type-filter button').each((i, typeButton) => {
                if (b.target !== typeButton) {
                    showSwirls(typeButton);
                }
            });
            currentFilter = null;
        } else {
            hideSwirls(currentFilter);
            showSwirls(b.target);
            currentFilter = b.target;
        }
        console.log('Current filter is', currentFilter);
    });
}


module.exports = {
    init: init
};