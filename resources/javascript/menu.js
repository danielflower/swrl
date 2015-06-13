var setup = function ($) {
    var header = $('.site-header');

    $('.menu-button').click(() => {
        header.toggleClass('menu-open');
        return false
    });
};

module.exports = {init: setup};