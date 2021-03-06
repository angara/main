
(function() {
  var MON_RU, add_marker, d02, ddmmyy_hhmm, mmmdd_hhmm, 
      st_balloon, st_hint, t_deg, update_st;

  d02 = function(d) {
    if (d < 10) {
      return "0" + d;
    } else {
      return "" + d;
    }
  };

  ddmmyy_hhmm = function(t) {
    if (!t) {
      return "??.??.?? ??:??";
    }
    return "" + 
      d02(t.getDate())+"."+d02(t.getMonth()+1)+"."+d02(t.getYear() - 100)+" " + 
      d02(t.getHours()) + ":" + d02(t.getMinutes());
  };

  MON_RU = [
    "Янв", "Фев", "Мар", "Апр", "Май", "Июн", "Июл", "Авг", "Сен", "Окт", "Ноя", "Дек"
  ];

  mmmdd_hhmm = function(t) {
    if (!t) {
      return "???.?? ??:??";
    }
    return "" + MON_RU[t.getMonth()] + "." + d02(t.getDate()) + " " + 
            d02(t.getHours()) + ":" + d02(t.getMinutes());
  };

  function meteo_link(st_id) {
    return "https://angara.net/meteo/st/"+st_id;
  }

  t_deg = function(t) {
    t = Math.round(t);
    return (t > 0 ? "+" + t : "" + t) + "&deg;";
  };

  st_hint = function(st) {
    return "<b style='font-size: 12px;'>" + st.title + "</b>"+"<br/>" + 
      "<span style='color:#77b; margin: auto 4px;'>" 
        + mmmdd_hhmm(new Date(st.last.ts)) + "</span>";
  };

  st_balloon = function(st) {
    return (
      "<div><a href='"+meteo_link(st._id)+"'>"+(st.addr || st.title || "")+"</a></div>" + 
      "<div style='padding: auto 8px; text-align: center;'>" + 
        "<span style='font-size: 15px;'><b>" + t_deg(st.last.t) + "</b>C</span>" + 
        "<span style='color:#77b; margin-left: 8px; font-size: 12px;'>" + 
          mmmdd_hhmm(new Date(st.last.ts)) + "</span>" + 
      "</div>");
  };

  add_marker = function(st) {
    return window.ymap.geoObjects.add(new ymaps.Placemark(st.ll, {
      id: st.id,
      type: 'meteo_marker',
      iconContent: t_deg(st.last.t),
      hintContent: st_hint(st),
      balloonContent: st_balloon(st),
      hideIcon: false
    }, {
      preset: 'twirl#lightblueStretchyIcon'
    }));
  };

  update_st = function(data) {
    var d, dm, i, j, len, len1, ymap;
    ymap = window.ymap;
    if (!((ymap != null) && data)) {
      return;
    }
    dm = {};
    for (i = 0, len = data.length; i < len; i++) {
      d = data[i];
      dm[d._id] = d;
    }
    ymap.geoObjects.each(function(gobj) {
      var st_id;
      if (gobj.properties.get("type") !== 'meteo_marker') {
        return;
      }
      st_id = gobj.properties.get('id');
      d = dm[st_id];
      if (!d) {
        ymap.geoObjects.remove(gobj);
        return;
      }
      gobj.properties.set('iconContent', t_deg(d.last.t));
      gobj.properties.set('hintContent', st_hint(d));
      gobj.properties.set('balloonContent', st_balloon(d));
      return d['updated'] = true;
    });
    for (j = 0, len1 = data.length; j < len1; j++) {
      d = data[j];
      if (!d.updated) {
        add_marker(d);
      }
    }
  };

  $(function() {
    var METEO, ymaps_api;
    ymaps_api = "https://api-maps.yandex.ru/2.0-stable/?lang=ru_RU" + 
                "&load=package.standard&coordorder=longlat";
    METEO = window.meteo;
    return $.getScript(ymaps_api, function() {
      return ymaps.ready(function() {
        var ymap;
        ymap = new ymaps.Map(METEO.map_div_id, {
          center: [104.276084, 52.270944],
          zoom: 10,
          type: 'yandex#hybrid'
        });
        window.ymap = ymap;
        ymap.controls.add('zoomControl');
        ymap.controls.add(new ymaps.control.TypeSelector());
        ymap.controls.add('mapTools');
        ymap.controls.add('scaleLine');
        METEO.update_st = update_st;
        if (METEO.last_st_data) {
          return update_st(METEO.last_st_data);
        }
      });
    });
  });

}).call(this);
