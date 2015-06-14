var setup = function ($) {
    var header = $('.site-header');

    $('.menu-button').click(() => {
        header.toggleClass('menu-open');
        return false
    });

    var path = location.pathname;
    $('nav li a[href="' + path + '"]').addClass('nav-selected');
};

module.exports = {init: setup};