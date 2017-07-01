//
//  Angara.Net: site commons
//

'use strict';

function fn_href(href) {
  return function() { window.location.href = href; }
}

function do_logout() {
  lib.post('/auth/logout', {}, function(resp){
     if(resp.redir){ window.location.href = resp.redir; }
   });
}

// .c-popmenu [data-popmenu]
//    .c-popmenu-toggle (click)
//    .c-popmenu-pane
//      ul > li|hr

function popmenu_toggle(evt)
{
  console.log("toggle:", evt);
  evt.preventDefault();
  var popmenu = $(evt.currentTarget).closest(".c-popmenu");
  if(!popmenu.length) {
    return;
  }
  var popmenu_pane = popmenu.find(".c-popmenu-pane");
  if(popmenu_pane.length) {
    // close pane
    popmenu_pane.remove();
    return;
  }

  // open pane
  var items = popmenu.data("popmenu");
  if(!items.length) {
    return;
  }

  var ul = $("<ul>");
  items.forEach(function(item){
    var action = item[0];
    var text = item[1];
    if( text == "-" ) {
      ul.append("<hr/>");
    }
    else {
      ul.append(
        $("<li>").html(text).data("action",action).click(function(evt){
          popmenu.remove(".c-popmenu-pane");
          var action = $(evt.currentTarget).data("action");
          if(typeof action === "function") {
            action.call();
          }
          else {
            window.location.href = action;
          }
        })
      )
    }
  });

  var pane = $("<div class='c-popmenu-pane'>");
  pane.css("top", popmenu.height());
  pane.append( ul );
  popmenu.append(pane);
}

$(function(){

  // c-popmenu
  $(".c-popmenu").each( function(i, el){
    $(el).find(".c-popmenu-toggle").click(popmenu_toggle);
  });
  $(document).on("keydown.popmenu",
    function(evt){
      if(evt.isDefaultPrevented()){ return; }
      if(evt.which == 27) {
        $(".c-popmenu-pane").remove();
      }
    }
  );
  $(document).on("click.popmenu", // touchstart.popmenu",
    function(evt){
      if(evt.isDefaultPrevented()){ return; }
      $(".c-popmenu-pane").remove();
    }
  );


  // search

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
