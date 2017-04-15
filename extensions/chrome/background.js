chrome.browserAction.onClicked.addListener(function (activeTab) {
    var url = encodeURIComponent(activeTab.url);
    var title = encodeURIComponent(activeTab.title);
    var newURL = 'https://www.swrl.co/create/from-url?url=' + url + '&title=' + title;
    chrome.tabs.create({ url: newURL });
});