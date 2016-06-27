//
//  Angara.Net: site commons
//

'use strict';

$(function(){

  var yasearch_id = "1682884";

  function yasearch(text) {
    if(text) {
      window.location.href =
        "https://yandex.ru/search/site/?searchid="+yasearch_id+
        "&l10n=ru&text="+ encodeURIComponent(text);
    }
  }

  var $search = $(".b-topbar .search");
  $search.keypress( function(evt) {
    if(evt.which == 13) {
      evt.preventDefault(); yasearch($search.val());
    }
  });
  $(".b-topbar .btn-search").click( function(evt) {
    evt.preventDefault(); yasearch($search.val());
  });

});
