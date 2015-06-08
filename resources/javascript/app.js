import '../../bower_components/es6-promise/promise.min.js';
import '../../bower_components/fetch/fetch.js';
import setupChromeExtension from "./chrome-extension";
import setupEditor from "./editor";
import setupSwirlEdit from "./edit-swirl";

$(document).ready(function () {
    setupEditor();
    setupSwirlEdit();
    setupChromeExtension();
});
