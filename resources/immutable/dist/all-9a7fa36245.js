!function t(e,n,i){function o(s,a){if(!n[s]){if(!e[s]){var l="function"==typeof require&&require;if(!a&&l)return l(s,!0);if(r)return r(s,!0);var c=new Error("Cannot find module '"+s+"'");throw c.code="MODULE_NOT_FOUND",c}var u=n[s]={exports:{}};e[s][0].call(u.exports,function(t){var n=e[s][1][t];return o(n||t)},u,u.exports,t,e,n,i)}return n[s].exports}for(var r="function"==typeof require&&require,s=0;s<i.length;s++)o(i[s]);return o}({1:[function(t,e,n){"use strict";function i(t){return t&&t.__esModule?t:{default:t}}var o=t("./chrome-extension"),r=i(o),s=t("./editor"),a=i(s),l=t("./edit-swirl"),c=i(l),u=t("./response-form"),d=i(u),f=t("./comment-form"),h=i(f),m=t("./menu"),p=i(m),v=t("./search"),g=i(v),b=t("./swirl-list"),w=i(b),y=t("./ga"),x=i(y);!function(){var t=function(t,e){t||document.write('<script type="text/javascript" src="/immutable/js/'+e+'"><\/script>')};t(window.Promise,"promise-6.0.2.min.js"),t(window.fetch,"fetch-2.0.3.js")}(),$(document).ready(function(){a.default.init($),a.default.initWidgets($),(0,c.default)(),(0,r.default)(),d.default.init($),h.default.init($),p.default.init($),w.default.init($),g.default.init($),x.default.addAnalyticsIfProd(),$(".show-notifications-button").click(function(){$(".notification li").css("display","list-item"),$(".show-notifications-button").hide()}),$(".expansion-content").hide(),$(".expand-toggle-button").click(function(t,e){$(t.currentTarget).closest(".expansion-area").find(".expansion-content").toggle(250),$(t.currentTarget).toggleClass("expanded")}),document.documentElement.className+="ontouchstart"in document.documentElement?" touch":" no-touch"})},{"./chrome-extension":2,"./comment-form":3,"./edit-swirl":4,"./editor":5,"./ga":6,"./menu":8,"./response-form":9,"./search":10,"./swirl-list":11}],2:[function(t,e,n){"use strict";var i=function(){var t=window.chrome;t&&t.app&&($(".chrome-only").css("display","block"),t.app.isInstalled?$(".chrome-extension-installed").css("display","block"):($(".install-chrome-extension-box").css("display","block"),$(".add-to-chrome-button").click(function(){console.log("running"),t.webstore.install(void 0,function(t){$(".chrome-extension-installed").css("display","block"),$(".install-chrome-extension-box").css("display","none"),console.log("Installation succeeded",t)},function(t){console.log("Installation failed",t)})})))};e.exports=i},{}],3:[function(t,e,n){"use strict";function i(t){return t&&t.__esModule?t:{default:t}}function o(t,e){if(!(t instanceof e))throw new TypeError("Cannot call a class as a function")}var r=function(){function t(t,e){for(var n=0;n<e.length;n++){var i=e[n];i.enumerable=i.enumerable||!1,i.configurable=!0,"value"in i&&(i.writable=!0),Object.defineProperty(t,i.key,i)}}return function(e,n,i){return n&&t(e.prototype,n),i&&t(e,i),e}}(),s=t("./http.js"),a=i(s),l=t("./editor.js"),c=i(l),u=function(){function t(e,n){var i=this;o(this,t),this.editor=new c.default.RichTextEditor(e(n).find("div.rte")),this.$form=e(n),this.$addButton=this.$form.find("input[type=submit]"),this.$maxCommentIdField=this.$form.find(".max-comment-id-field"),this.swirlId=parseInt(e(n).find(".swirl-id-field").val(),10),this.refreshToken=null,e(n).submit(function(){i.setLoading();var t=i.editor.getHtmlContent();return console.log("Going to post comment",t),t&&t.trim().length>0?a.default.post("/swirls/"+i.swirlId+"/comment",{comment:t}).then(i.addMissingComments.bind(i)).then(function(){i.resetForm()}):(console.warn("No HTML found in editor",i.editor),window.alert("Oops, your comment could not be posted. Please try again.")),!1}),this.addMissingComments()}return r(t,[{key:"addMissingComments",value:function(){this.refreshToken&&clearTimeout(this.refreshToken);var t=this;return a.default.getJson("/swirls/"+t.swirlId+"/comments?comment-id-start="+t.$maxCommentIdField.val()).then(function(e){e.count>0&&($(".comments").append(e.html),t.$maxCommentIdField.val(e.maxId)),t.refreshToken=setTimeout(t.addMissingComments.bind(t),3e4)})}},{key:"setLoading",value:function(){this.$addButton.addClass("button-loading"),this.$addButton.prop("disabled",!0)}},{key:"resetForm",value:function(){this.$addButton.prop("disabled",!1),this.$addButton.removeClass("button-loading"),this.editor.clear()}}]),t}(),d=function(t){t("form.comment").each(function(e,n){return new u(t,n)})};e.exports={init:d}},{"./editor.js":5,"./http.js":7}],4:[function(t,e,n){"use strict";var i=function(){var t=function(t){for(var e=t.value,n=t.getAttribute("list"),i=document.querySelectorAll("#"+n+" option"),o=!1,r=0;r<i.length;r++){var s=i[r];if(s.innerText===e){o=s.getAttribute("data-value");break}}if(o)$(t).before(o);else{var a=$(document.createElement("input")).attr("type","checkbox").attr("name","who").attr("value",e).attr("checked","checked").attr("id",e),l=$(document.createElement("label")).attr("for",e).attr("class","no-avatar");$(t).before(a),$(t).before(l),l.append(document.createTextNode(e))}var c=$(document.createElement("div")).attr("class","small-padding");$(t).before(c),t.value=""};$(".user-select-box input").keydown(function(e){if(13==e.which||9==e.which)return e.preventDefault(),t(this),!1}),$(".change-image-link").click(function(t){return $(".change-image-area").removeClass("start-hidden"),$(t.target).addClass("start-hidden"),!1})};e.exports=i},{}],5:[function(t,e,n){"use strict";function i(t,e){if(!(t instanceof e))throw new TypeError("Cannot call a class as a function")}var o=function(){function t(t,e){for(var n=0;n<e.length;n++){var i=e[n];i.enumerable=i.enumerable||!1,i.configurable=!0,"value"in i&&(i.writable=!0),Object.defineProperty(t,i.key,i)}}return function(e,n,i){return n&&t(e.prototype,n),i&&t(e,i),e}}(),r=t("./http.js"),s=function(t){return t&&t.__esModule?t:{default:t}}(r),a=function(){function t(e){var n=this;i(this,t),this.$textarea=e.find("textarea").first(),this.$editorDiv=e.find(".editor").first(),this.editorDiv=this.$editorDiv[0],e.find(".spoiler-alert-button").click(function(){return n.addHtmlAtCursor('<div class="spoiler-alert"><div class="spoiler-alert--bar" title="Click to expand" contenteditable="false"><button class="spoiler-alert--close-button" title="Delete spoiler alert">x</button><a href="#">Spoiler alert</a></div><div class="spoiler-alert--content" data-ph="Write your spoilers here - they will not be shown unless clicked on"></div></div><p data-ph="..."></p>'),$(".spoiler-alert--close-button").click(function(t){return $(t.target).closest(".spoiler-alert").remove(),!1}),!1});var o=this;this.$editorDiv.bind("paste",function(){setTimeout(o.convertPlainTextLinksToAnchorTags.bind(o),1)});var r=this.$textarea.val();this.$editorDiv.html(r),e.closest("form").on("submit",function(){return n.copyInputFromEditableDivToPostableTextArea(),!0}),this.$editorDiv.focusout(function(){n.copyInputFromEditableDivToPostableTextArea()})}return o(t,[{key:"copyInputFromEditableDivToPostableTextArea",value:function(){this.$textarea.val(this.getHtmlContent())}},{key:"visitDescendents",value:function(t,e){for(var n=t.childNodes,i=0;i<n.length;i++){var o=n[i];e(o)&&this.visitDescendents(o,e)}}},{key:"enrichLinks",value:function(){this.$editorDiv.find("a").each(function(t,e){e.href===e.innerText&&"transforming-link"!==e.className&&(e.className="transforming-link",$(e).append('<i class="fa fa-spin fa-spinner"></i>'),s.default.getJson("/website-service/get-metadata?url="+encodeURI(e.href)).then(function(t){console.log("Got metadata",t),e.innerText=t.title||e.href,e.className="";var n=t["embed-html"];n&&$(e).after('<div class="user-entered-embed-box">'+n+"</div>")}))})}},{key:"convertPlainTextLinksToAnchorTags",value:function(){var t=this.editorDiv,e=this.getHtmlContent().match(/(http(s|):\/\/[^<>\s]+(\.[^<>\s]+)*(|:[0-9]+)[^<>\s]*)/g);e&&(this.visitDescendents(t,function(t){if(3===t.nodeType){for(var n=0;n<e.length;n++){var i=$("<div/>").html(e[n]).text(),o=t.data.indexOf(i);if(o>-1){var r=t.splitText(o);r.splitText(i.length);var s=0===i.indexOf("https://www.swrl.co")?"":' target="_blank"';return $(r).before('<a href="'+i+'"'+s+">"+i+"</a>"),r.data=" ",!1}}window.todo||(window.todo=t)}else if(1===t.nodeType&&"A"===t.tagName)return!1;return!0}),this.enrichLinks())}},{key:"addHtmlAtCursor",value:function(t){this.$editorDiv.append(t)}},{key:"getHtmlContent",value:function(){return this.$editorDiv.html().trim()}},{key:"clear",value:function(){this.$textarea.val(""),this.$editorDiv.html("")}}]),t}(),l=function(t){t(".rte").each(function(e,n){new a(t(n))})},c=function(t){t(".spoiler-alert--bar a").click(function(e){return t(e.target).closest(".spoiler-alert").find(".spoiler-alert--content").toggle(),!1}),t(".spoiler-alert--close-button").click(function(e){return t(e.target).closest(".spoiler-alert").remove(),!1})};e.exports={init:l,RichTextEditor:a,initWidgets:c}},{"./http.js":7}],6:[function(t,e,n){"use strict";var i=function(){"www.swrl.co"===document.location.hostname&&(!function(t,e,n,i,o,r,s){t.GoogleAnalyticsObject=o,t[o]=t[o]||function(){(t[o].q=t[o].q||[]).push(arguments)},t[o].l=1*new Date,r=e.createElement(n),s=e.getElementsByTagName(n)[0],r.async=1,r.src="//www.google-analytics.com/analytics.js",s.parentNode.insertBefore(r,s)}(window,document,"script",0,"ga"),ga("create","UA-63844233-1","auto"),ga("send","pageview"))};e.exports={addAnalyticsIfProd:i}},{}],7:[function(t,e,n){"use strict";var i=function(t){return fetch("/api/v1"+t,{credentials:"same-origin",headers:{Accept:"application/json"}}).then(function(t){return t.json()})},o=function(t,e){return fetch("/api/v1"+t,{method:"post",credentials:"same-origin",headers:{Accept:"application/json","Content-Type":"application/json"},body:JSON.stringify(e)})};e.exports={getJson:i,post:o}},{}],8:[function(t,e,n){"use strict";var i=function(t){var e=t("body");t(".menu-button").click(function(){return e.toggleClass("menu-open"),window.scrollTo(0,0),!1})};e.exports={init:i}},{}],9:[function(t,e,n){"use strict";function i(t,e){if(!(t instanceof e))throw new TypeError("Cannot call a class as a function")}var o=function(){function t(t,e){for(var n=0;n<e.length;n++){var i=e[n];i.enumerable=i.enumerable||!1,i.configurable=!0,"value"in i&&(i.writable=!0),Object.defineProperty(t,i.key,i)}}return function(e,n,i){return n&&t(e.prototype,n),i&&t(e,i),e}}(),r=t("./http.js"),s=function(t){return t&&t.__esModule?t:{default:t}}(r),a=function(){function t(e,n){var o=this;i(this,t),this.$form=e(n),this.response=null,e(n).find("button").click(this.buttonClick.bind(this));var r=e(n).find(".custom-response");e(n).submit(function(){var t=parseInt(e(n).find(".swirl-id-field").val(),10),i=o.getResponse();return e(r).val(""),i&&(o.setSelectedButton(i,"button-loading"),s.default.post("/swirls/"+t+"/respond",{responseButton:i}).then(function(){o.setSelectedButton(i,"swirl-button")})),!1}),r.keydown(function(t){if(13==t.which||9==t.which)return e(n).find(".custom-response-button").click(),!1})}return o(t,[{key:"setSelectedButton",value:function(t,e){var n=!1,i=null;if(this.$form.find("button").each(function(o,r){$(r).removeClass("swirl-button"),$(r).removeClass("button-loading"),r.value.toLowerCase()===t.toLowerCase()?(n=!0,$(r).addClass(e)):i||(i=r)}),!n){var o=$(i).clone(!0);o.val(t).addClass(e),o.text(t),this.$form.find(".response-buttons").append(o)}}},{key:"buttonClick",value:function(t){"custom"===t.target.getAttribute("data-button-type")?this.response=$(t.target.form).find(".custom-response").val():this.response=t.target.value}},{key:"getResponse",value:function(){return(this.response||"").trim()}}]),t}(),l=function(t){t(".respond-form").each(function(e,n){return new a(t,n)})};e.exports={init:l}},{"./http.js":7}],10:[function(t,e,n){"use strict";function i(t){t(".search-form .query").keydown(function(e,n){r&&window.clearTimeout(r),r=window.setTimeout(function(){t(".search-form").submit()},300)}),t(".search-form").submit(function(e){var n=t(e.currentTarget),i=n.find(".query").val();o=i,n.find(".search-result-summary").toggleClass("no-query",!i),i||(i="");var r=n.find(".submit");n.find(".query-val").text(i);var s=n.find(".result-count");s.html('<i class="fa fa-spin fa-spinner"></i>');var a="/api/v1/swirls/search?query="+encodeURIComponent(i);return fetch(a,{credentials:"same-origin"}).then(function(t){if(200!==t.status)throw"from "+a+": "+t.status+" "+t.statusText;return t.text()}).then(function(e){o===i&&(t(".search-results").html(e),s.html(t(".search-results .mini-swirl").length),r.html("Go"),requestAnimationFrame(function(){t(".pending-to-appear").css("opacity","1.0")}),history.replaceState({query:i},"Search results for "+i,"/search?query="+encodeURIComponent(i)))}).catch(function(t){console.log("Error while getting search results",t),n.off(),n.submit()}),!1})}var o=null,r=null;window.onpopstate=function(t){var e=(t.state?t.state.query:"")||"";$(".search-form .query").val(e),$(".search-form").submit()},e.exports={init:i}},{}],11:[function(t,e,n){"use strict";function i(t){t(window).scroll(function(){t(window).scrollTop()+t(window).height()+320>=t(document).height()&&t(".more-swirls-button").click()}),t(".more-swirls-button").click(function(e){var n=t(e.target);if(n.attr("data-disabled"))return!1;var i=n.attr("data-ids").split(","),o=parseInt(n.attr("data-num-loads"),10),r=parseInt(n.attr("data-per-page"),10),s=o*r,a=s+r,c=i.slice(s,a),u=parseInt(n.attr("data-start-index"),10),d=n.attr("data-url-prefix")+(u+a+r),f=n.attr("data-url-prefix")+(u+a),h=n.text();if(0===c.length)return!0;n.attr("data-disabled","true"),n.html('<i class="fa fa-spin fa-spinner"></i> Loading');var m="/api/v1/swirls?swirl-list="+c.join(",");return fetch(m,{credentials:"same-origin"}).then(function(t){if(200!==t.status)throw"from "+m+": "+t.status+" "+t.statusText;return t.text()}).then(function(e){t(".swirl-insertion-point").before(e),l(t),setTimeout(function(){t(".pending-to-appear").css("opacity","1.0")},20),n.removeAttr("data-disabled"),n.text(h),n.attr("href",d)}).catch(function(t){console.log("Error while getting more",t),location.href=f}),n.attr("data-num-loads",o+1),!1}),t(".type-filter button").click(function(e){var n=e.target.getAttribute("data-swirl-type");s=n===s?null:n,l(t)});var e=function(e){var n=t(e)[0].parentNode.parentNode,i=n.getAttribute("data-title"),o=n.getAttribute("data-image-url"),s=n.getAttribute("data-review"),a=n.getAttribute("data-swirl-type");t(n).remove(),r.default.post("/swirls/create-swirl",{title:i,review:s,type:a,imageUrl:o})};t(".swirl-list").on("click","i.add-to-wishlist-button",function(){e(this)}),t(".swirl-list").on("click","i.dismiss-button",function(){a(this,"Dismissed")}),t(".swirl-list").on("click","i.later-button",function(){a(this,"Later")})}var o=t("./http.js"),r=function(t){return t&&t.__esModule?t:{default:t}}(o),s=null,a=function(t,e){var n=$(t)[0].parentNode.parentNode,i=n.getAttribute("id");$(n).remove(),r.default.post("/swirls/"+i+"/respond",{responseButton:e})},l=function(t){t(".type-filter button").each(function(e,n){var i=n.getAttribute("data-swirl-type"),o=null==s||s===i;t(n).toggleClass("hidden",!o),t(".swirl."+i).toggle(o)})};e.exports={init:i}},{"./http.js":7}]},{},[1]);