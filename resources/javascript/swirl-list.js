var currentFilter = null;

var showSwirls = function (button) {
    var swirlType = button.getAttribute('data-swirl-type');
    $(button).toggleClass('hidden', false);
    $('.mini-swirl.' + swirlType).show();
    $('.mini-swirl.' + swirlType).removeClass('hidden');
};

var hideSwirls = function (button) {
    var swirlType = button.getAttribute('data-swirl-type');
    $(button).toggleClass('hidden', true);
    $('.mini-swirl.' + swirlType).hide();
    $('.mini-swirl.' + swirlType).addClass('hidden');
};

var fillInHiddenSwirls = function (chosenSwirlType) {
    var nextSwirlOptions = document.querySelectorAll('#more-swirls option');
    var allSwirlsCount = document.getElementsByClassName('mini-swirl').length;
    var hiddenCount = document.getElementsByClassName('mini-swirl hidden').length;
    var missingCount = 20 - allSwirlsCount + hiddenCount;
    var swirlList = document.getElementsByClassName('swirl-list')[0];

    for(var i = 0; i < nextSwirlOptions.length; i++) {
        if(missingCount === 0){
            break;
        }
        var option = nextSwirlOptions[i];
        if(option.getAttribute('data-swirl-type') === chosenSwirlType){
            var nextSwirlHTML = option.getAttribute('data-value');
            var nextSwirlID = option.innerText;
            $(swirlList).append(nextSwirlHTML);
            var swirlAdded = document.getElementById(nextSwirlID)
            $(swirlAdded).addClass('added');
            missingCount--;
        }
    }
}

var removeAddedSwirls = function (){
    $('.mini-swirl.added').remove();
}

function init($) {
    $('.type-filter button').click((b) => {
        removeAddedSwirls();
        var chosenSwirlType = b.target.getAttribute('data-swirl-type');
        if (currentFilter == null) {
            $('.type-filter button').each((i, typeButton) => {
                if (b.target !== typeButton) {
                    hideSwirls(typeButton);
                }
            });
            fillInHiddenSwirls(chosenSwirlType);
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
            fillInHiddenSwirls(chosenSwirlType);
            currentFilter = b.target;
        }
    });
}


module.exports = {
    init: init
};