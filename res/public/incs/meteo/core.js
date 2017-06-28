//
//  angara.net/meteo
//

$(function() {
  var METEO_URL = "/meteo1";
  var METEO_HOURLY = "//api.angara.net/meteo/st/hourly?st=";

  var ST_COOKIE = 'meteo_st';

  function save_st_cookie(s) {
    document.cookie = ST_COOKIE+"="+encodeURIComponent(s)+";"+
      " path=/; expires=Fri, 01 Jan 2100 00:00:00 GMT;"
  };

  function save_st_list(arr, reload) {
    console.log("save:", arr);
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

  function move_card_to(i, pos, arr)
  {
    if( pos >= arr.length ) { pos = arr.length-1; }
    if( pos < 0 ) { pos = 0; }

    if( pos < i ) {
      return [].concat(
        arr.slice(0, pos), [arr[i]], arr.slice(pos, i), arr.slice(i+1)
      );
    }
    else if( pos > i ) {
      return [].concat(
        arr.slice(0, i), arr.slice(i+1, pos+1), [arr[i]], arr.slice(pos+1)
      );
    }
    else {
      return arr;
    }
  }

  function card_remove(i, arr) {
    return [].concat( arr.slice(0,i), arr.slice(i+1) );
  }

  /// /// /// ///


  function remove_menu($card) {
    $card.find(".i-menu").remove();
  }

  function make_menu(i, $card) {
    var mn = $("<div class='i-menu'>");
    // mn.append(
    //   $("<div class='text-right'><i class='fa fa-caret-up i-toggle'/></div>")
    //     .click(function(evt) {
    //       $(evt.target).closest(".i-menu").remove();
    //     })
    // );
    mn.append(
      $("<ul>")
        .append(
          $("<li><i class='fa fa-fw fa-angle-double-up'/> В начало</li>").click(
            function(evt) {
              remove_menu($card);
              save_st_list( move_card_to(i, 0, st_list()), true );
            }
          )
        )
        .append(
          $("<li><i class='fa fa-fw fa-angle-up'/> Вверх</li>").click(
            function(evt) {
              console.log("up");
              remove_menu($card);
              save_st_list( move_card_to(i, i-1, st_list()), true );
            }
          )
        )
        .append(
          $("<li><i class='fa fa-fw fa-angle-down'/> Вниз</li>").click(
            function(evt) {
              console.log("down");
              remove_menu($card);
              save_st_list( move_card_to(i, i+1, st_list()), true );
            }
          )
        )
        .append(
          $("<li><i class='fa fa-fw fa-angle-double-down'/> В конец</li>").click(
            function(evt) {
              remove_menu($card);
              save_st_list( move_card_to(i, 9999, st_list()), true );
            }
          )
        )
        .append("<hr style='margin: 5px 3px'/>")
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
    $card.find(".title")
      .append(
        $("<span class='cog'><i class='fa fa-fw fa-caret-down'></i></span>")
          .click(
            function(evt) {
              evt.preventDefault();
              if(! $card.find(".i-menu").length ) {
                $(".b-card .i-menu").remove();
                $card.append( make_menu(i, $card) );
              }
              else {
                $(".b-card .i-menu").remove();
              }
            }
          )
      );
  });

  /// /// /// ///  svg  /// /// /// ///

  function get_hourly() {
    if(window.hourly_t0 && window.hourly_t1) {
      var st = st_list();
      $.getJSON(
        METEO_HOURLY+st.join(',')
          +"&t0="+window.hourly_t0.toISOString()
          +"&t1="+window.hourly_t1.toISOString()
        ,
        function(resp) {
          console.log("hd:", resp);
          if(resp.ok) {

          }
        }
      );
    }
  };

  get_hourly();

});



//.
