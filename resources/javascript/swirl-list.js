
var hidden = {};

function init($) {
  $('.type-filter button').click((b) => {
      var swirlType = b.target.getAttribute('data-swirl-type');
      var wasHidden = !!hidden[swirlType];
      hidden[swirlType] = !wasHidden;
      $(b.target).toggleClass('hidden', !wasHidden);
      if (wasHidden) {
          $('.mini-swirl.' + swirlType).show();
      } else {
          $('.mini-swirl.' + swirlType).hide();
      }
  });
}

module.exports = {
    init: init
};