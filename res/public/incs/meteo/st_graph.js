//
//  angara.net/meteo st_graph
//

$(function() {
  var METEO_HOURLY = "//api.angara.net/meteo/st/hourly?st=";
  var HPA_MMHG = 1.3332239;


  var DIV_ID = "st_graph";

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
          cb(t0, resp.series[st]);
        }; // no else
      }
    );
  }


  function graph_month(t0, data)
  {
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
    
        Highcharts.chart(DIV_ID, {
          title: { text: "" },
          legend: { enabled: false },
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
              pointStart: t0.getTime() + window.tz_offset_millis,
              pointInterval: 3600 * 1000 // one hour
            },
            areaspline:{
              fillColor: {
                linearGradient: {
                    x1: 0,
                    y1: 0,
                    x2: 0,
                    y2: 1
                },
                stops: [
                    [0, Highcharts.getOptions().colors[0]],
                    [1, Highcharts.Color(Highcharts.getOptions().colors[0]).setOpacity(0).get('rgba')]
                ]
              },
              // marker: {
              //     radius: 2
              // },
              lineWidth: 1
              // states: {
              //     hover: {
              //         lineWidth: 1
              //     }
              // },            
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
              visible: true,
              opposite: true,
              title: {enabled: false},
              labels: {              
                format: '{value} м/с',
                style: {
                  color: "#000088"
                }
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
                //
                lineWidth: 1,
                color: '#FF0000',
                negativeColor: '#0088FF',
                // lineColor: '#303030',
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
              lineWidth: 1,
              dashStyle: "Solid",
              color: "#000099",
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
      resetZoom: "Сбросить",
      resetZoomTitle: "Используйте Shift для горизонтальной прокрутки"
  	}
  });


  // // // // // // 

  function set_active_year_month() {
    $("#"+DIV_ID).html("<div class='loading'>Загрузка графика ...</div>");
    get_month_hourly(window.st_year, window.st_month, graph_month);
  }

  $(".j_year").change(function(evt){
    window.st_year = $(evt.target).val();
    set_active_year_month();
  });

  $(".j_month").click(function(evt){
    var $btn = $(evt.target);
    $(".j_month").removeClass("btn-curr");
    $btn.addClass("btn-curr");
    window.st_month = $btn.data("month");
    set_active_year_month();
  });


  // // // // // //  on-load  // // // // // // 

  $(".j_month").each(function(i, el){
    var month = window.st_month;
    var $el = $(el);
    if($el.data("month") == month) {
      $el.addClass("btn-curr");
      return false;
    };
  });

  $(".j_year option").each(function(i, el) {
    var year = window.st_year;
    var $opt = $(el);
    if($opt.attr("value") == year) {
      $opt.attr("selected", true);
      return false;
    };
  })
  
  set_active_year_month();

});

//.
