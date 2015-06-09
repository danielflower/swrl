import '../../bower_components/es6-promise/promise.min.js';
import '../../bower_components/fetch/fetch.js';
import setupChromeExtension from "./chrome-extension";
import setupEditor from "./editor";
import setupSwirlEdit from "./edit-swirl";
import swirlView from "./swirl-view";

$(document).ready(function () {
    setupEditor();
    setupSwirlEdit();
    setupChromeExtension();
    swirlView.init($);
});
