import http from './http.js';

var currentFilter = null;
var chosenSwirlType = null;
var currentPagingNumber = 0;
var indexesFetchedFromPaging = [];
var lastIndexFetchedFromFilter = null;
var p1IndexFetched = null;
var p2IndexFetched = null;

var scrollToTop = function () {
    $('html, body').animate({ scrollTop: $("#page-title").offset().top }, 'fast');
};

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
    var swirlList = document.getElementById('swirl-list');

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

var respondAndRemove = function (element, response){
     var swirlElement = $(element)[0].parentNode.parentNode;
     var swirlID = swirlElement.getAttribute('id');
     var wasAdded = $(swirlElement).hasClass('added');
     $(swirlElement).remove();
     var nextSwirlOption = document.querySelectorAll('#more-swirls option')[0];
     if(nextSwirlOption != null){
         var nextSwirlID = nextSwirlOption.innerText;
         var nextSwirlHTML = nextSwirlOption.getAttribute('data-value');
         var swirlList = document.getElementById('swirl-list');
         $(swirlList).append(nextSwirlHTML);
         if(wasAdded){
         // then the Swirl it is replacing was added and we should mark the new Swirl as 'added' too
          $(document.getElementById(nextSwirlID)).addClass('added'); //FIXME: This isn't working and I don't know why
         }
         $(nextSwirlOption).remove();
     }
     http.post('/swirls/' + swirlID + '/respond', {responseButton: response});
}

function init($) {
    $('button.previous-swirls').hide();
    $('.type-filter button:NOT(.expand-toggle-button)').click((b) => {
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
        scrollToTop();
    });

    $('button.previous-swirls').click((b) => {
         if(currentFilter == null){
             // no filter, so just restore the previous page as per the paging number
             removeAddedSwirls();
             currentPagingNumber--;
             if(currentPagingNumber === 0){
                 restoreToInitialState();
             }else {
                 fillInHiddenSwirls('all', 20 * (currentPagingNumber - 1));
             }
         } else {
             // filter applied, so restore the previous page as per the tracked index.
             if(indexesFetchedFromPaging.length === 1){
             // means we need to restore the initial page and apply the original filter
                 restoreToInitialState();
                 $('.type-filter button').each((i, typeButton) => {
                     if (currentFilter !== typeButton) {
                         hideSwirls(typeButton);
                     }
                 });
                 fillInHiddenSwirls(chosenSwirlType, 0);
             } else {
             // means we should restore to the previous index marker
                 removeAddedSwirls();
                 indexesFetchedFromPaging.pop();
                 fillInHiddenSwirls(chosenSwirlType, indexesFetchedFromPaging[indexesFetchedFromPaging.length - 1]);
             }
         }
         scrollToTop();
    })

    $('#swirl-list').on('click', 'i.dismiss-button', function(){
        respondAndRemove(this, 'Dismissed');
    });

    $('#swirl-list').on('click', 'i.later-button', function(){
        respondAndRemove(this, 'Later');
    });
}


module.exports = {
    init: init
};