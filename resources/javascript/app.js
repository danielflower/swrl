import setupChromeExtension from './chrome-extension';
import editor from './editor';
import setupSwirlEdit from './edit-swirl';
import responseForm from './response-form';
import commentForm from './comment-form';
import menu from './menu';
import search from './search';
import swirlList from './swirl-list';
import ga from './ga';

(function () {
    const fill = function (skip, filename) {
        if (!skip) {
            document.write('<scr' + 'ipt type="text/javascript" src="/immutable/js/' + filename + '"></scr' + 'ipt>');
        }
    };
    fill(window.Promise, 'promise-6.0.2.min.js');
    fill(window.fetch, 'fetch-2.0.3.js');
})();

$(document).ready(function () {
    editor.init($);
    editor.initWidgets($);
    setupSwirlEdit();
    setupChromeExtension();
    responseForm.init($);
    commentForm.init($);
    menu.init($);
    swirlList.init($);
    search.init($);
    ga.addAnalyticsIfProd();

    $('.show-notifications-button').click(() => {
        $('.notification li').css('display', 'list-item');
        $('.show-notifications-button').hide();
    });

    $('.expansion-content').hide();

    $('.expand-toggle-button').click((e, i) => {
        var content = $(e.currentTarget).closest('.expansion-area').find('.expansion-content');
        content.toggle(250);
        $(e.currentTarget).toggleClass('expanded');
    });

    document.documentElement.className +=
        (("ontouchstart" in document.documentElement) ? ' touch' : ' no-touch');

});
