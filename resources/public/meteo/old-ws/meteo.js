
(function() {
  var METEO, REFRESH_INTERVAL, _fetch_st_interval, fetch_st_data;

  window.meteo = {
    map_div_id: "mapdiv",
    st_url: "/meteo/old-ws/st",
    last_st_data: null
  };

  METEO = window.meteo;

  REFRESH_INTERVAL = 300 * 1000;

  _fetch_st_interval = null;

  fetch_st_data = function() {
    return $.getJSON(METEO.st_url).success(function(data) {
      METEO.last_st_data = data;
      if (METEO.update_st != null) {
        return METEO.update_st(data);
      }
    });
  };

  $(function() {
    fetch_st_data();
    return _fetch_st_interval = setInterval(fetch_st_data, REFRESH_INTERVAL);
  });

}).call(this);
