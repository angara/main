//
//  angara.net/meteo
//

$(function() {
  var METEO_URL = "/meteo1";
  var METEO_HOURLY = "//api.angara.net/meteo/st/hourly?st=";

  // !!! // METEO_HOURLY = "/api/meteo/st/hourly?st=";

  var ST_COOKIE = 'meteo_st';
  var HPA_MMHG = 1.3332239;


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
              remove_menu($card);
              save_st_list( move_card_to(i, i-1, st_list()), true );
            }
          )
        )
        .append(
          $("<li><i class='fa fa-fw fa-angle-down'/> Вниз</li>").click(
            function(evt) {
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

  function draw_graph (id, t0, data) {
    var t_series = [], p_series = [], h_series = [], w_series = [];

    for(var i in data) {
      var d = data[i];
      // t:
      if(d && d.t) { t_series.push(Math.round(d.t.avg)); }
      else { t_series.push(null); }
      // p:
      if(d && d.p) { p_series.push(Math.round(d.p.avg / HPA_MMHG)); }
      else { p_series.push(null); }
      // h:
      if(d && d.h) { h_series.push(Math.round(d.h.avg)); }
      else { h_series.push(null); }
      // w:
      if(d && d.w) { w_series.push(Math.round(d.w.avg)); }
      else { w_series.push(null); }
    }

    Highcharts.chart(id, {
      title: { text: "" },
      legend: { enabled: false },
      //
      plotOptions: {
        series: {
          className: 'main-color',
          negativeColor: true,
          pointStart: t0.getTime(),
          pointInterval: 3600 * 1000 // one hour
        }
      },
      //
      tooltip: {
        xDateFormat: "<b>%e %B %Y -- %H:%M</b>",
        shared: true
      },
      //
      xAxis: [{
        type: "datetime",
        crosshair: true
      }],
      //
      yAxis: [
        {
          // t, idx:0
          crosshair: true,
          labels: {
              format: '{value}°',
              style: {
                  color: "#aa0044"
              }
          },
          title: { enabled: false }
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
            min: 700,
            max: 730,
            opposite: true
        },
        {
          // h
          visible: false,
          min: 0,
          max: 100
        },
        {
          // w
          visible: false,
          min: 0,
          max: 20
        }
      ],
      //
      series: [
        {
            name: 'Температура',
            type: 'area',
            yAxis: 0,
            data: t_series,
            //
            color: '#FF0000',
            negativeColor: '#0088FF',
            // lineColor: '#303030',
            // lineWidth: 1,
            // fillColor: {
            //   linearGradient: [0, 0, 0, 300],
            //   stops: ["#000000", "#4488ff"]
            // },
            tooltip: { valueSuffix: ' °C' }
        },
        {
            name: 'Давление',
            type: 'spline',
            yAxis: 1,
            data: p_series,
            // marker: {
            //     enabled: false
            // },
            dashStyle: 'shortdot',
            tooltip: { valueSuffix: ' мм.рс' }
        },
        {
          name: "Влажность",
          type: 'spline',
          data: h_series,
          yAxis: 2,
          tooltip: { valueSuffix: ' %' }
        },
        {
          name: "Сила ветра",
          type: 'spline',
          data: w_series,
          yAxis: 3,
          tooltip: { valueSuffix: ' м/с' }
        }
      ],
      credits: { enabled: false }
    });
    // highcharts

  }


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
            $(".b-card[data-st]").each(function(i, el){
              var st = $(el).data("st");
              var data = resp.series[st];
              var graph_id = $(el).find(".graph").attr("id");
              if(graph_id && data) {
                draw_graph(graph_id, window.hourly_t0_utc, data);
              }
            });
          } // no else
        }
      );
    }
  };

  Highcharts.setOptions({
  	lang: {
      shortMonths: [
        'Янв','Фев','Мар','Апр','Май','Июн','Июл','Авг','Сен','Окт','Ноя','Дек'
      ],
      months: [
        "января", "февраля", "марта", "апреля", "мая", "июня",
        "июля", "августа", "сентября", "октября", "ноября", "декабря"
      ]
  	}
  });

  get_hourly();

});


//.
