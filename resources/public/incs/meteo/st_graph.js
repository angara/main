//
//  angara.net/meteo st_graph
//

$(function() {
  var METEO_HOURLY = "/meteo/st-hourly/";
  var HPA_MMHG = 1.3332239;

  var DIV_ID = "st_graph";

  function get_month_hourly(year, month, cb)
  {
    var st = window.st_id;
    var tz_ofs = window.tz_offset_millis;
    var t0 = new Date(Date.UTC(year, month-1, 1) - tz_ofs);
    var t1 = new Date( ((month == 12)? Date.UTC(1+year, 0, 1): Date.UTC(year,month,1)) - tz_ofs );

    $.getJSON(
      METEO_HOURLY+st+"?&ts_beg="+t0.toISOString()+"&ts_end="+t1.toISOString(),
      function(resp) {
        if(resp.series) {
          cb(Date.UTC(year, month-1, 1), resp);
        };
      }
    );
  }


  function graph_month(t0_ms, data)
  {
    var t_series = data.series.t || [], 
        p_series = data.series.p || [], 
        w_series = data.series.w || []
        ;

        for(let i=0; i < t_series.length; i++) {
          if(t_series[i]) {
            t_series[i] = Math.round(t_series[i]);
          }
        }
    
        for(let i=0; i < p_series.length; i++) {
          if(p_series[i]) {
            p_series[i] = Math.round(p_series[i] / HPA_MMHG);
          }
        }

        for(let i=0; i < w_series.length; i++) {
          if(w_series[i]) {
            w_series[i] = Math.round(w_series[i]);
          }
        }

        let p_max = Math.max(...p_series);
        if(p_max && p_max > 100) {
          p_max = Math.ceil(p_max/50) * 50;
        }
        else {
          p_max = 750;
        }
        let p_min = p_max - 150;

        var chart = Highcharts.chart(DIV_ID, {
            title: { text: "" },
            legend: { enabled: true },
            chart: { 
              zoomType: 'x',
              panning: true,
              panKey: 'shift'            
            },
            //
            plotOptions: {
              series: {
                className: 'main-color',
                negativeColor: true,
                lineWidth: 1,
                pointStart: t0_ms,
                pointInterval: 3600 * 1000,
                fillOpacity: 0.6,
                marker: { radius: 3, symbol: "circle" }                
              },
              areaspline:{
                fillColor: {
                  linearGradient: { x1: 0, y1: -1, x2: 0, y2: 1 },
                  stops: [
                    [0, "#ff0000"],
                    [1, "#ffffff"],
                    [2, "#0000ff"]
                  ]
                }
              }
            },
            //
            tooltip: {
              xDateFormat: "<b>%e %B %Y -- %H:%M</b>",
              // followPointer: false,
              // positioner: function () {
              //   return { x: 54, y: 2 };  // chart.plotWidth
              // },
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
                    style: { color: "#903" }
                },
                title: { enabled: false },
                min: -50,
                max: 50
              },
              {
                gridLineWidth: 0,
                title: { enabled: false },
                labels: {
                  format: '{value} мм',
                  style: { color: "#2a2" }
                },
                min: p_min, 
                max: p_max,
                opposite: true
              },
              {
                // h
                visible: false,
                min: 0,
                max: 200,
              },
              {
                // w
                gridLineWidth: 0,
                visible: true,
                opposite: true,
                title: {enabled: false},
                labels: {              
                  format: '{value} м/с',
                  style: { color: "#24b" }
                },
                min: 0,
                max: 20
              }
            ],
            //
            series: [
              {
                  name: 'Температура',
                  type: 'areaspline',
                  yAxis: 0,
                  data: t_series,
                  marker: { radius: 3 },
                  color: '#FF6622',
                  negativeColor: '#2266FF',
                  tooltip: { valueSuffix: ' °C' },
                  zones: [
                    {value: -30, color: "#00f"},
                    {value: -20, color: "#02f"},
                    {value: -10, color: "#24f"},
                    {value:  -5, color: "#48f"},
                    {value:   0, color: "#4af"},
                    {value:   5, color: "#fa4"},
                    {value:  10, color: "#f84"},
                    {value:  20, color: "#f42"},
                    {value:  30, color: "#f00"}
                  ]
              },
              {
                  name: 'Давление',
                  type: 'spline',
                  yAxis: 1,
                  data: p_series,
                  zIndex: 10,
                  color: "#2c4",
                  marker: { radius: 3 },
                  tooltip: { valueSuffix: ' мм.рст' }
              },
              {
                name: "Ветер",
                type: 'column',
                data: w_series,
                color: "#6ae",
                yAxis: 3,
                tooltip: { valueSuffix: ' м/с' }
              }
            ],
            credits: { enabled: false }
          });
          // highcharts
  } // graph_month

  Highcharts.setOptions({
  	lang: {
      shortMonths: [
        'Янв','Фев','Мар','Апр','Май','Июн','Июл','Авг','Сен','Окт','Ноя','Дек'
      ],
      months: [
        "января", "февраля", "марта", "апреля", "мая", "июня",
        "июля", "августа", "сентября", "октября", "ноября", "декабря"
      ],
      resetZoom: "Начальный масштаб",
      resetZoomTitle: "Используйте Shift для горизонтальной прокрутки"
  	}
  });


  // // // // // // 

  function set_active_year_month() {
    var year  = +window.st_year;
    var month = +window.st_month;
    
    if(window.history) {
      window.history.pushState(
        null, $("title").text(), window.location.pathname+"?year="+year+"&month="+month
      );
    }

    $(".j_month").each(function(i, el){
      var $el = $(el);
      var mn = +$el.data("month");
      $el.removeClass("btn-curr");
      if(mn == month) {
        $el.addClass("btn-curr");
      };

      $el.prop("disabled", (Date.UTC(year, mn-1, 1) > window.now_ms));
    });
  
    //
    $("#"+DIV_ID).html("<div class='loading'>Загрузка графика ...</div>");
    get_month_hourly(year, month, graph_month);
  }

  $(".j_year").change(function(evt){
    window.st_year = +$(evt.target).val();
    set_active_year_month();
  });

  $(".j_month").click(function(evt){
    window.st_month = +$(evt.target).data("month");
    set_active_year_month();
  });


  // // // // // //  on-load  // // // // // // 


  var year = window.st_year;
  $(".j_year option").each(function(i, el) {
    var $opt = $(el);
    if($opt.attr("value") == year) {
      $opt.prop("selected", true);
      return false;
    };
  })
  
  set_active_year_month();

});
