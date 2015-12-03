import http from './http.js';

var currentFilter = null;
var chosenSwirlType = null;
var currentPagingNumber = 0;
var indexesFetchedFromPaging = [];
var lastIndexFetchedFromFilter = null;
var p1IndexFetched = null;
var p2IndexFetched = null;

var hideSwirls = function (button) {
    var swirlType = button.getAttribute('data-swirl-type');
    $(button).toggleClass('hidden', true);
    $('.mini-swirl.' + swirlType).hide();
    $('.mini-swirl.' + swirlType).addClass('hidden');
};

var fillInHiddenSwirls = function (chosenSwirlType, start) {
    var nextSwirlOptions = document.querySelectorAll('#more-swirls option');
    var allSwirlsCount = document.getElementsByClassName('mini-swirl').length;
    var hiddenCount = document.getElementsByClassName('mini-swirl hidden').length;
    var missingCount = 20 - allSwirlsCount + hiddenCount;
    var swirlList = document.getElementsByClassName('swirl-list')[0];

    for(var i = start; i < nextSwirlOptions.length; i++) {
        if(missingCount === 0){
            break;
        }
        var option = nextSwirlOptions[i];
        if(chosenSwirlType === 'all' || option.getAttribute('data-swirl-type') === chosenSwirlType){
            var nextSwirlHTML = option.getAttribute('data-value');
            var nextSwirlID = option.innerText;
            $(swirlList).append(nextSwirlHTML);
            var swirlAdded = document.getElementById(nextSwirlID);
            $(swirlAdded).addClass('added');
            lastIndexFetchedFromFilter = i;
            missingCount--;
        }
    }
}

var removeAddedSwirls = function (){
    $('.mini-swirl.added').remove();
}

var restoreToInitialState = function(){
    removeAddedSwirls();
    $('.mini-swirl').show();
    $('.mini-swirl').removeClass('hidden');
    currentPagingNumber = 0;
    lastIndexFetchedFromFilter = null;
    indexesFetchedFromPaging = [];
    $('button.previous-swirls').hide();
}

function init($) {
    $('button.previous-swirls').hide();
    $('.type-filter button').click((b) => {
        restoreToInitialState(); // restore paging back to first page
        chosenSwirlType = b.target.getAttribute('data-swirl-type');
        if (currentFilter == null || currentFilter !== b.target) {
            $('.type-filter button').each((i, typeButton) => {
                if (b.target !== typeButton) {
                    hideSwirls(typeButton);
                }
            });
            $(b.target).toggleClass('hidden', false);
            fillInHiddenSwirls(chosenSwirlType, 0);
            currentFilter = b.target;
        } else {
            $('.type-filter button').each((i, typeButton) => {
                if (b.target !== typeButton) {
                    $(typeButton).toggleClass('hidden', false);
                }
            });
            currentFilter = null;
            chosenSwirlType = null;
        }
    });

    $('button.next-swirls').click((b) => {
        if (currentFilter == null){
            // no filter, so just grab the next 20 swirls and keep track of how many pages
            removeAddedSwirls();
            $('.mini-swirl').hide();
            $('.mini-swirl').addClass('hidden');
            fillInHiddenSwirls('all', 20 * currentPagingNumber);
            currentPagingNumber++;
            $('button.previous-swirls').show();
        } else {
            // filter applied, so only grab chosen type and keep track of the last index
            removeAddedSwirls();
            $('.mini-swirl').hide();
            $('.mini-swirl').addClass('hidden');
            if(lastIndexFetchedFromFilter == null){
            // the filter didn't need to grab any new swirls
                indexesFetchedFromPaging.push(0);
            } else {
                indexesFetchedFromPaging.push(lastIndexFetchedFromFilter + 1);
            }
            fillInHiddenSwirls(chosenSwirlType, indexesFetchedFromPaging[indexesFetchedFromPaging.length - 1]);
            $('button.previous-swirls').show();
        }
    });

    $('button.previous-swirls').click((b) => {
         if(currentFilter == null){
             // no filter, so just restore the previous page as per the paging number
             removeAddedSwirls();
             currentPagingNumber--;
             if(currentPagingNumber === 0){
                 restoreToInitialState();
                 $('html, body').animate({ scrollTop: $("#page-title").offset().top }, 'fast');
             }else {
                 fillInHiddenSwirls('all', 20 * (currentPagingNumber - 1));
             }
         } else {
             // filter applied, so restore the previous page as per the tracked p2 index.
             if(indexesFetchedFromPaging.length === 1){
             // means we need to restore the initial page and apply the original filter
                 restoreToInitialState();
                 $('.type-filter button').each((i, typeButton) => {
                     if (currentFilter !== typeButton) {
                         hideSwirls(typeButton);
                     }
                 });
                 fillInHiddenSwirls(chosenSwirlType, 0);
                 $('html, body').animate({ scrollTop: $("#page-title").offset().top }, 'fast');
             } else {
             // means we should restore to the previous index marker
                 removeAddedSwirls();
                 indexesFetchedFromPaging.pop();
                 fillInHiddenSwirls(chosenSwirlType, indexesFetchedFromPaging[indexesFetchedFromPaging.length - 1]);
             }
         }
    })

    $('button.dismiss-button').click((b) => {
         var swirlElement = b.target.parentNode.parentNode;
         var swirlID = swirlElement.getAttribute('id');
         console.log(swirlID);
         http.post('/swirls/' + swirlID + '/respond', {responseButton: 'dismissed'}).then(() => {
             $(swirlElement).remove();
             var nextSwirlOption = document.querySelectorAll('#more-swirls option')[0];
             var nextSwirlHTML = nextSwirlOption.getAttribute('data-value');
             var swirlList = document.getElementsByClassName('swirl-list')[0];
             $(swirlList).append(nextSwirlHTML);
             $(nextSwirlOption).remove();
         });
    });
}


module.exports = {
    init: init
};