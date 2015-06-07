var setupChromeExtension = function () {
    var chrome = window.chrome;
    if (chrome && chrome.app) {
        $('.chrome-only').css('display', 'block');

        if (chrome.app.isInstalled) {
            // Note: this always returns false even when installed
            $('.chrome-extension-installed').css('display', 'block');
        } else {
            $('.install-chrome-extension-box').css('display', 'block');
            $('.add-to-chrome-button').click(function () {
                console.log('running');
                chrome.webstore.install(undefined,
                    function (suc) {
                        $('.chrome-extension-installed').css('display', 'block');
                        $('.install-chrome-extension-box').css('display', 'none');
                        console.log('Installation succeeded', suc);
                    },
                    function (err) {
                        console.log('Installation failed', err);
                    }
                );
            });
        }
    }
};
module.exports = setupChromeExtension;