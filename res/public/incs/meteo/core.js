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
      var st_comma = c.join(",")
      save_st_cookie(st_comma);
      window.location.href = METEO_URL; // +"?st="+st_comma;
    }
  });
});

//.
