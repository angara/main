//
//  angara.net/meteo
//

$(function() {
  var METEO_URL = "/meteo1";
  var ST_COOKIE = 'meteo_st';

  // function
  console.log("c:", document.cookie);

  document.cookie = "meteo_st="+encodeURIComponent("uiii,uibb,npsd");

  function save_st_cookie(s) {
    console.log("st_c:", s);
    document.cookie = ST_COOKIE+"="+encodeURIComponent(s);
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
    console.log("st");
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
