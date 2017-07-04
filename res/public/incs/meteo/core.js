//
//  angara.net/meteo
//

$(function() {
  var METEO_URL = "/meteo1";
  var METEO_HOURLY = "//api.angara.net/meteo/st/hourly?st=";

  // !!! ///
  METEO_HOURLY = "/api/meteo/st/hourly?st=";


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


  var HPA_MMHG = 1.3332239;

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


  $(document).on("keydown.meteo.core",
    function(evt){
      if(evt.isDefaultPrevented()) { return; }
      if(evt.which == 27) {
        $(".b-card .i-menu").remove();
      }
    }
  );
  $(document).on("click.meteo.core",
    function(evt){
      if(evt.isDefaultPrevented()) { return; }
      $(".b-card .i-menu").remove();
    }
  );

  /// /// /// ///  graphs  /// /// /// ///

  function draw_graph (st, data) {
    var t_series = [];
    var p_series = [];

    console.log("st:", st, data);

    for(var i in data) {
      var d = data[i];
      if(d) {
        if(d.t) { t_series.push(Math.round(d.t.avg)); }
        else { t_series.push(null); }
        //
        if(d.p) {
          p_series.push(Math.round(d.p.avg / HPA_MMHG));
        }
        else { p_series.push(null); }
      }
      else {
        t_series.push(null);
        p_series.push(null);
      }
    }

    Highcharts.chart('graph_'+st, {
        // chart: { zoomType: 'xy' },
        title: { text: "" },
        // subtitle: {
        //     text: 'Source: WorldClimate.com'
        // },
        credits: { enabled: false },
        xAxis: [{
            // categories: [
            //     'Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun',
            //     'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'
            //   ],
            crosshair: true
        }],
        //
        yAxis: [
        {
            // crosshair: true,
            labels: {
                format: '{value}°',
                style: {
                    // color: Highcharts.getOptions().colors[2]
                    color: "#aa0044"
                }
            },

            title: {
              enabled: false
                // text: 'Температура, °C',
                // style: {
                //   //  color: Highcharts.getOptions().colors[2]
                //   color: "#22cc22"
                // }
            }
        },
        {
            gridLineWidth: 0,
            // crosshair: true,
            title: {
              enabled: false
                // text: 'Давление, мм.рт.ст',
                // style: {
                //   color: "#2244ff"
                // }
            },
            labels: {
                format: '{value} мм',
                // style: {
                //     color: Highcharts.getOptions().colors[0]
                // }
            },
            opposite: true
        }],
        tooltip: {
            shared: true
        },
        legend: {
          enabled: false
            // layout: 'vertical',
            // align: 'left',
            // x: 80,
            // verticalAlign: 'top',
            // y: 20,
            // floating: true,
            // backgroundColor: (Highcharts.theme && Highcharts.theme.legendBackgroundColor) || '#FFFFFF'
        },
        series: [
        {
            name: 't',
            type: 'column',
            yAxis: 0,
            data: t_series,
            tooltip: {
              valueSuffix: ' °C'
            }

        },
        {
            name: 'p',
            type: 'spline',
            yAxis: 1,
            data: p_series,
            marker: {
                enabled: false
            },
            dashStyle: 'shortdot',
            tooltip: {
              valueSuffix: ' мм.рт.ст'
            }
        }]
    });
    // highcharts

  }


  // Highcharts.chart('chart', {
  //   chart: {
  //       type: 'bar'
  //   },
  //   title: {
  //       text: 'Fruit Consumption'
  //   },
  //   xAxis: {
  //       categories: ['Apples', 'Bananas', 'Oranges']
  //   },
  //   yAxis: {
  //       title: {
  //           text: 'Fruit eaten'
  //       }
  //   },
  //   series: [{
  //       name: 'Jane',
  //       data: [1, 0, 4]
  //   }, {
  //       name: 'John',
  //       data: [5, 7, 3]
  //   }]
  // });


  // // // //

  function get_hourly() {
    if(window.hourly_t0 && window.hourly_t1) {
      var st = st_list();
      $.getJSON(
        METEO_HOURLY+st.join(',')
          +"&t0="+window.hourly_t0.toISOString()
          +"&t1="+window.hourly_t1.toISOString()
        ,
        function(resp) {
          if(resp.ok) {
            for(var st in resp.series) {
              draw_graph(st, resp.series[st]);
            }
          }
        }
      );
    }
  };

  get_hourly();

});


//.
