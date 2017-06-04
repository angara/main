//
//  angara.net/meteo
//

$(function() {
  var METEO_URL = "/meteo1";
  var ST_COOKIE = 'meteo_st';

  function save_st_cookie(s) {
    document.cookie = ST_COOKIE+"="+encodeURIComponent(s)+";"+
      " path=/; expires=Fri, 01 Jan 2100 00:00:00 GMT;"
  };

  function save_st_list(arr, reload) {
    save_st_cookie(arr.join(","));
    if(reload) {
      window.location.href = METEO_URL; // +"?st="+st_comma;
    }
  };

  function st_list() {
    var c = [];
    $("div.b-card[data-st]").each(function(i, el){
      c.push( $(el).data("st") );
    });
    return c;
  };

  $("#btn_st_add").click(function(evt){
    var st = $("#st_list").val();
    if( st ) {
      var c = st_list();
      c.push(st);
      save_st_list(c, true);
    }
  });


  /// /// /// ///

  function card_to_begin(i, arr) {
    // console.log("beg:", arr);
    return [].concat( [arr[i]], arr.slice(0,i), arr.slice(i+1) );
  }

  function card_to_end(i, arr) {
    // console.log("end:", arr);
    return [].concat( arr.slice(0,i), arr.slice(i+1), [arr[i]] );
  }

  function card_remove(i, arr) {
    // console.log("rem:", arr);
    return [].concat( arr.slice(0,i), arr.slice(i+1) );
  }

  /// /// /// ///


  function remove_menu($card) {
    $card.find(".i-menu").remove();
  }

  function make_menu(i, $card) {
    var mn = $("<div class='i-menu'>");
    mn.append(
      $("<div class='text-right'><i class='fa fa-caret-up i-toggle'/></div>")
        .click(function(evt) {
          $(evt.target).closest(".i-menu").remove();
        })
    );
    mn.append(
      $("<ul>")
        .append(
          $("<li><i class='fa fa-fw fa-caret-up'/> В начало</li>").click(
            function(evt) {
              remove_menu($card);
              save_st_list( card_to_begin(i, st_list()), true );
            }
          )
        )
        .append(
          $("<li><i class='fa fa-fw fa-caret-down'/> В конец</li>").click(
            function(evt) {
              remove_menu($card);
              save_st_list( card_to_end(i, st_list()), true );
            }
          )
        )
        .append(
          $("<li><i class='fa fa-fw fa-remove'/> Удалить</li>").click(
            function(evt) {
              remove_menu($card);
              save_st_list( card_remove(i, st_list()), true );
            }
          )
        )
    );
    return mn;
  }


  $("div.b-card[data-st]").each( function(i, el){
    var $card = $(el);
    $card.find(".title").click(
      function(evt) {
        $(".b-card .i-menu").remove();
        $card.append( make_menu(i, $card) );
      }
    );
  });

});

//.
