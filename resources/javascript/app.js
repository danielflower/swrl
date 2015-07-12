import '../../bower_components/es6-promise/promise.min.js';
import '../../bower_components/fetch/fetch.js';
import setupChromeExtension from './chrome-extension';
import editor from './editor';
import setupSwirlEdit from './edit-swirl';
import responseForm from './response-form';
import commentForm from './comment-form';
import menu from './menu';
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
    ga.addAnalyticsIfProd();

    !function(d,s,id){var js,fjs=d.getElementsByTagName(s)[0],p=/^http:/.test(d.location)?'http':'https';if(!d.getElementById(id)){js=d.createElement(s);js.id=id;js.src=p+'://platform.twitter.com/widgets.js';fjs.parentNode.insertBefore(js,fjs);}}(document, 'script', 'twitter-wjs');
});
