import '../../bower_components/es6-promise/promise.min.js';
import '../../bower_components/fetch/fetch.js';
import setupChromeExtension from './chrome-extension';
import editor from './editor';
import setupSwirlEdit from './edit-swirl';
import responseForm from './response-form';
import commentForm from './comment-form';
import menu from './menu';
import search from './search';
import swirlList from './swirl-list';
import ga from './ga';

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
    !function (d, s, id) {
        var js, fjs = d.getElementsByTagName(s)[0], p = /^http:/.test(d.location) ? 'http' : 'https';
        if (!d.getElementById(id)) {
            js = d.createElement(s);
            js.id = id;
            js.src = p + '://platform.twitter.com/widgets.js';
            fjs.parentNode.insertBefore(js, fjs);
        }
    }(document, 'script', 'twitter-wjs');
    !(function (d, s, id) {
        var js, fjs = d.getElementsByTagName(s)[0];
        if (d.getElementById(id)) return;
        js = d.createElement(s);
        js.id = id;
        js.src = "//connect.facebook.net/en_GB/sdk.js#xfbml=1&version=v2.4&appId=893395944039576";
        fjs.parentNode.insertBefore(js, fjs);
    }(document, 'script', 'facebook-jssdk'));

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
