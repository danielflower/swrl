import setupChromeExtension from "./chrome-extension";
import setupEditor from "./editor";
import setupSwirlEdit from "./edit-swirl";

$(document).ready(function () {
    setupEditor();
    setupSwirlEdit();
    setupChromeExtension();
});
