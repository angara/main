//
//  angara.net/meteo st_graph
//

$(function() {
  var METEO_HOURLY = "//api.angara.net/meteo/st/hourly?st=";
  var HPA_MMHG = 1.3332239;

  function get_month_hourly(year, month, cb)
  {
    var st = window.st_id;
    var t0 = new Date(year, (month-1), 1);
    var t1 = (month == 12)?
                new Date(year+1, 0, 1) :
                new Date(year, month, 1);

    $.getJSON(
      METEO_HOURLY+st+"&t0="+t0.toISOString()+"&t1="+t1.toISOString(),
      function(resp) {
        if(resp.ok) {
          cb(resp.data);
          // $(".b-card[data-st]").each(function(i, el){
          //   var st = $(el).data("st");
          //   var data = resp.series[st];
          //   var graph_id = $(el).find(".graph").attr("id");
          //   if(graph_id && data) {
          //     draw_graph(graph_id, window.hourly_t0_utc, data);
          //   }
          });
        } // no else
      }
    );
  }
};


  }

  function graph_month(data)
  {

  }

  get_month_hourly(st_year, st_month, graph_month)
});

//.
