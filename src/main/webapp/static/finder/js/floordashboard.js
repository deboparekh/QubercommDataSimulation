var imageW	  = 40;
var imageH	  = 40;
var fzie = 30;
var txty = 9;
(function () {
	search = window.location.search.substr(1)
	urlObj=JSON.parse('{"' + decodeURI(search).replace(/"/g, '\\"').replace(/&/g, '","').replace(/=/g,'":"') + '"}')
    //var timer = 500000;
	//var count = 1;
	var peerStats;
	var counterIncrement = 0;
	var counterIncrement1 = 0;
	var counterIncrement2 = 0; 
    FloorDashboard = {
        timeoutCount: 10000,
        charts: {
            urls: {
                txRx:'txrx' , //'/facesix/rest/beacon/ble/networkdevice/rxtx?spid='+urlObj.spid,
                activeConnections:'/facesix/rest/beacon/ble/networkdevice/peercount?spid='+urlObj.spid+"&cid="+urlObj.cid,//Todo Url has to be changed here 
                netFlow: 'netflow' ,//'/facesix/rest/beacon/ble/networkdevice/flraggr?spid='+urlObj.spid,//Todo Url has to be changed here
                typeOfDevices: '/facesix/rest/beacon/ble/networkdevice/venue/connectedTagType?spid='+urlObj.spid+"&cid="+urlObj.cid,
                devicesConnected: 'gettags',//'/facesix/rest/beacon/ble/networkdevice/gettags?spid='+urlObj.spid+"&cid="+urlObj.cid,//Todo Url has to be changed here                
                avgUplinkSpeed: 'exittag',//'/facesix/rest/beacon/ble/networkdevice/exittag?spid='+urlObj.spid,
                avgDownlinkSpeed: '/facesix/rest/beacon/ble/networkdevice/inactiveTagsCount?spid='+urlObj.spid+"&cid="+urlObj.cid,
                idle: '/facesix/rest/beacon/ble/networkdevice/idleTagsCount?spid='+urlObj.spid+"&cid="+urlObj.cid,
            },
            setChart: {
                txRx: function (initialData,params) {
                	var duration = params;
                	var len		 = duration?duration.length:0;
                	var link 	 = FloorDashboard.charts.urls.txRx;

                	
                	//console.log ("Txrx Floor");
                	
                    $.ajax({
                        url: link,
                        success: function (result) {
                        	//console.log (result);
                            if (result && result.length) {                                
                                var timings = [];
                                var txArr = ["Tx"];
                                var rxArr = ["Rx"];
                                for (var i = 1; i < result.length; i++){   
                                	if (len == 0) {
	  						        	if (result[i].Tx == undefined) {
							        		continue;
							        	}
							        	if (result[i].Rx == undefined) {
							        		continue;
							        	}
							        	if (result[i].time == undefined) {
							        		continue;
							        	}                                  
	            						txArr.push(result[i].Tx);
	            						rxArr.push(result[i].Rx);
	            						txArr[i] = txArr[i]/100;
	            						rxArr[i] = rxArr[i]/100;
            						} else {
            							txArr[i] = result[i].max_vap_tx_bytes/100;
            							rxArr[i] = result[i].max_vap_rx_bytes/100;           						
            						}
            						var formatedTime = result[i].time;
            						var c_formatedTime = formatedTime.substr(0, 10) + "T" + formatedTime.substr(11, 8);
            						c_formatedTime = new Date (c_formatedTime);
                                    timings.push(c_formatedTime.getHours() + ":" + c_formatedTime.getMinutes());
                                }
                                FloorDashboard.charts.chartConfig.txRx.data.columns = [txArr, rxArr];
                                FloorDashboard.charts.chartConfig.txRx.axis.x.categories = timings;
                            }
                            if (initialData) {
                                FloorDashboard.charts.getChart.txRx = c3.generate(FloorDashboard.charts.chartConfig.txRx);
                                //FloorDashboard.charts.setChart.avgDownlinkSpeed(true);
                                //FloorDashboard.charts.setChart.avgUplinkSpeed(true);
                            } else {
                                //FloorDashboard.charts.setChart.avgDownlinkSpeed();
                                //FloorDashboard.charts.setChart.avgUplinkSpeed();
                                FloorDashboard.charts.getChart.txRx.load({ "columns": FloorDashboard.charts.chartConfig.txRx.data.columns, "categories":  FloorDashboard.charts.chartConfig.txRx.axis.x.categories});
                            }
                            FloorDashboard.charts.getChart.txRx = c3.generate(FloorDashboard.charts.chartConfig.txRx);
                            setTimeout(function () {
                             FloorDashboard.charts.setChart.txRx();
                            }, FloorDashboard.timeoutCount);
                        },
                        error: function (data) {
                            //console.log(data);
                            setTimeout(function () {
                              FloorDashboard.charts.setChart.txRx();
                            }, FloorDashboard.timeoutCount);
                        },
                        dataType: "json"
                    });
                },
                activeConnections: function (initialData,params) {
                    $.ajax({
                        url:FloorDashboard.charts.urls.activeConnections,
                        success: function (result) {
                        	//console.log("activeConnections " +result);
                            FloorDashboard.charts.chartConfig.activeConnections.targetPos = targetPos = result;
                            FloorDashboard.charts.chartConfig.activeConnections.innerHTML = '<i class="fa fa-tags"></i></br>0';
                            //if (initialData) {
                           //  $('#demo-pie-2').circles(FloorDashboard.charts.chartConfig.activeConnections);
                            counter=0; 
                            //var timer=setInterval(function(){
                            // var pieChart=$("#demo-pie-2").data("circles");
                           // var str = '<i class="fa fa-tags" aria-hidden="true" style="color:green"></i></br>'
                           // var str = str + result;
                           // pieChart.innerhtml.html(str);
                            $('#tagsIn').html(result);
                            if(counterIncrement == 0){
                            	 $('#tagsIn').each(function () {
                                     $(this).prop('Counter',0).animate({
                                         Counter: $(this).text()
                                     }, {
                                         duration: 2000,
                                         easing: 'swing',
                                         step: function (now) {
                                             $(this).text(Math.ceil(now));
                                         }
                                     });
                                 });
                            	counterIncrement = 1;
                            }
                           
                            
                            /*   if(counter>=targetPos) {
                            	counter = 0;
                                clearInterval(timer);
                            }
                            else{
                            		counter+=1;
                            		pieChart.innerhtml.text(counter.toString())
                                	
                                }*/
                            //},100)
                            //}
                            setTimeout(function () {
                             FloorDashboard.charts.setChart.activeConnections();
                            }, FloorDashboard.timeoutCount);
                        },
                        error: function (data) {
                            //console.log(data);
                           setTimeout(function () {
                             FloorDashboard.charts.setChart.activeConnections();
                           }, FloorDashboard.timeoutCount);
                        },
                        dataType: "json"
                    });
                },
                netFlow: function (initialData,params) {
                	var duration = params;
                	var len		 = duration?duration.length:0;
                	var link 	 = FloorDashboard.charts.urls.netFlow;
                	
                	
                    $.ajax({
                        url:link,
                        success: function (result) {
                        	//console.log(result);
							var timings = [];
                            var radio 	= ["radio"];
                            var ulink 	= ["Uplink"];
                            var dlink 	= ["Downlink"];
                            for (var i = 0; i < result.length; i++){   
                                if (result[i].max_vap_rx_bytes < 0) {
        							result[i].max_vap_rx_bytes = 0;
        						}
        						
        						if (result[i].max_vap_tx_bytes < 0) {
        							result[i].max_vap_tx_bytes = 0; 
        						} 
        						  
                            	radio.push(result[i].Radio);
                            	if (result[i].Radio == "BLE") {
                            		ulink.push(Math.round(result[i].avg_ble_tx_bytes/100));
                            		dlink.push(Math.round(result[i].avg_ble_rx_bytes/100)); 
                            	}
    							ulink.push(Math.round(result[i].max_vap_rx_bytes/100));
    							dlink.push(Math.round(result[i].max_vap_tx_bytes/100));   						           						
                            }                         
                        
                            if (result) {

                                FloorDashboard.charts.chartConfig.netFlow.data.columns = [radio, ulink, dlink];
                                //FloorDashboard.charts.chartConfig.netFlow.axis.x.categories = timings;
                                FloorDashboard.charts.getChart.netFlow = c3.generate(FloorDashboard.charts.chartConfig.netFlow);
                            }
                            setTimeout(function () {
                              FloorDashboard.charts.setChart.netFlow();
                            }, FloorDashboard.timeoutCount);
                        },
                        error: function (data) {
                            //console.log(data);
                            setTimeout(function () {
                              FloorDashboard.charts.setChart.netFlow();
                           }, FloorDashboard.timeoutCount);
                        },
                        dataType: "json"
                    });
                },
                typeOfDevices: function (initialData) {
	                 
                	 $.ajax({
                         url: FloorDashboard.charts.urls.typeOfDevices,
                         success: function (result) {
                             var result=result.connectedInterfaces;
                             var columns=[];
                             var names={};
                             var colors={},colorMap={
                                 'enabled':"#6baa01",
                                 'disabled':'#cccccc'
                             }
                             
                             for(var i = 0; i< result.length; i++){
                                    columns.push([result[i].device,result[i].vapcount]);
                                    names["data"+(i+1)]=result[i].device;
                                    colors[result[i].device]=colorMap[result[i].status];
                             }                            	

                             FloorDashboard.charts.chartConfig.typeOfDevices.data.columns = columns;
                             FloorDashboard.charts.chartConfig.typeOfDevices.data.names   = names;
                             FloorDashboard.charts.chartConfig.typeOfDevices.data.colors  = colors;
                         	 FloorDashboard.charts.getChart.typeOfDevices = c3.generate(FloorDashboard.charts.chartConfig.typeOfDevices);
                             if (initialData) {
                             	FloorDashboard.charts.getChart.typeOfDevices = c3.generate(FloorDashboard.charts.chartConfig.typeOfDevices);
                             } else {
                             	FloorDashboard.charts.getChart.typeOfDevices.load({ "columns": FloorDashboard.charts.chartConfig.typeOfDevices.data.columns,'colors': FloorDashboard.charts.chartConfig.typeOfDevices.data.colors});
                             }                        	
                         	

                             setTimeout(function () {
                             	FloorDashboard.charts.setChart.typeOfDevices();
                             }, FloorDashboard.timeoutCount);
                             
                         },
                         error: function (data) {
                            //console.log(data);
                            setTimeout(function () {
                         	   FloorDashboard.charts.setChart.typeOfDevices();
                            }, FloorDashboard.timeoutCount);
                            
                         },
                         dataType: "json"
                     });

                },
                /*devicesConnected: function (initialData,params) {
                    $.ajax({
                        url:FloorDashboard.charts.urls.devicesConnected,
                        success: function (result) {
                        	peerStats = result.devicesConnected;
                        	//console.log ("Length" + peerStats.length)
                        	
                        	if (peerStats.length == 4) {
                        		FloorDashboard.charts.chartConfig.devicesConnected.data.columns = [result.devicesConnected[1] , 
                        																		   result.devicesConnected[2] ,
                        																		   result.devicesConnected[3]];
                        	} else {
                                FloorDashboard.charts.chartConfig.devicesConnected.data.columns = [result.devicesConnected[1], 
									   result.devicesConnected[2], 
									   result.devicesConnected[3], 
									   result.devicesConnected[4],
									   result.devicesConnected[5]]                        		
                        	}

                            FloorDashboard.charts.getChart.devicesConnected = c3.generate(FloorDashboard.charts.chartConfig.devicesConnected);
                            //FloorDashboard.charts.setChart.typeOfDevices(true);
                            setTimeout(function () {
                              FloorDashboard.charts.setChart.devicesConnected();
                            }, FloorDashboard.timeoutCount);
                        },
                        error: function (data) {
                            //console.log(data);
                            setTimeout(function () {
                            	FloorDashboard.charts.setChart.devicesConnected();
                            }, FloorDashboard.timeoutCount);                           
                        },
                        dataType: "json"
                    });
                },*/
                /*avgUplinkSpeed: function (initialData) {
                    $.ajax({
                        url:FloorDashboard.charts.urls.avgUplinkSpeed,
                        success: function (result) {
                        	//console.log("exit tag " +result);
                            FloorDashboard.charts.chartConfig.avgUplinkSpeed.targetPos = targetPos = result;
                            FloorDashboard.charts.chartConfig.avgUplinkSpeed.innerHTML = '<i class="fa fa-tags"></i></br>0';
                            if (initialData) {
                                $('#downchart').circles(FloorDashboard.charts.chartConfig.avgUplinkSpeed);
                                counter=0;
                                var timer=setInterval(function(){
                                    var pieChart=$('#downchart').data("circles");
                                    var str = '<i class="fa fa-tags" aria-hidden="true" style="color:red"></i></br>'
                                    var str = str + result;
                                    pieChart.innerhtml.html(str);
                                    if(counter>=targetPos) {
                                    	counter = 0;
                                        clearInterval(timer);
                                    }
                                    else{
                                    		counter+=1;
                                    		pieChart.innerhtml.text(counter.toString())
                                        	
                                        }
                                },100)
                            } else {
                                var pieChart = $('#downchart').data('circles');
                                pieChart.moveProgress(FloorDashboard.charts.chartConfig.avgUplinkSpeed.targetPos);
                            }
                           setTimeout(function () {
                             FloorDashboard.charts.setChart.avgUplinkSpeed();
                           }, FloorDashboard.timeoutCount);
                        },
                        error: function (data) {
                            //console.log(data);
                           setTimeout(function () {
                             FloorDashboard.charts.setChart.avgUplinkSpeed();
                           }, FloorDashboard.timeoutCount);
                        },
                        dataType: "json"
                    });
                },*/
                avgDownlinkSpeed: function (initialData) {
                    $.ajax({
                        url:FloorDashboard.charts.urls.avgDownlinkSpeed,
                        success: function (result) {
                        	//console.log("Inactive tags " +result);
                            FloorDashboard.charts.chartConfig.avgDownlinkSpeed.targetPos = targetPos = result;
                            FloorDashboard.charts.chartConfig.avgDownlinkSpeed.innerHTML = '<i class="fa fa-tags" style="color:red;"></i></br>0';
                            //if (initialData) {
                          // $('#upchart').circles(FloorDashboard.charts.chartConfig.avgDownlinkSpeed);
                            counter=0;
                            //var timer=setInterval(function(){
                              //  var pieChart=$('#upchart').data("circles");
                               // var str = '<i class="fa fa-tags" aria-hidden="true" style="color:red;"></i></br>'
                               // var str = str + result;
                               // pieChart.innerhtml.html(str);
                            	$('#tagsInactive').html(result);
                            	if(counterIncrement2 == 0){
                               	 $('#tagsInactive').each(function () {
                                        $(this).prop('Counter',0).animate({
                                            Counter: $(this).text()
                                        }, {
                                            duration: 2000,
                                            easing: 'swing',
                                            step: function (now) {
                                                $(this).text(Math.ceil(now));
                                            }
                                        });
                                    });
                               	counterIncrement2 = 1;
                               }
                             /*   if(counter>=targetPos) {
                                	counter = 0;
                                    clearInterval(timer);
                                }
                                else{
                                		counter+=1;
                                		pieChart.innerhtml.text(counter.toString())
                                    	
                                    }*/
                            //},100)
                           //}
                           setTimeout(function () {
                             FloorDashboard.charts.setChart.avgDownlinkSpeed();
                           }, FloorDashboard.timeoutCount);
                        },
                        error: function (data) {
                            //console.log(data);
                           setTimeout(function () {
                             FloorDashboard.charts.setChart.avgDownlinkSpeed();
                           }, FloorDashboard.timeoutCount);
                        },
                        dataType: "json"
                    });
                	
                },
                idle: function (initialData) {
                    $.ajax({
                        url:FloorDashboard.charts.urls.idle,
                        success: function (result) {
                        	//console.log("Inactive tags " +result);
                            FloorDashboard.charts.chartConfig.idle.targetPos = targetPos = result;
                            FloorDashboard.charts.chartConfig.idle.innerHTML = '<i class="fa fa-tags" style="color:yellow;"></i></br>0';
                            //if (initialData) {
                            // $('#idlechart').circles(FloorDashboard.charts.chartConfig.idle);
                            counter=0;
                            //var timer=setInterval(function(){
                               // var pieChart=$('#idlechart').data("circles");
                               // var str = '<i class="fa fa-tags" aria-hidden="true" style="color:orange;"></i></br>'
                              //  var str = str + result;
                              //  pieChart.innerhtml.html(str);
                             $('#tagsIdle').html(result);
                             if(counterIncrement1 == 0){
                            	 $('#tagsIdle').each(function () {
                                     $(this).prop('Counter',0).animate({
                                         Counter: $(this).text()
                                     }, {
                                         duration: 2000,
                                         easing: 'swing',
                                         step: function (now) {
                                             $(this).text(Math.ceil(now));
                                         }
                                     });
                                 });
                            	counterIncrement1 = 1;
                            }
                           setTimeout(function () {
                             FloorDashboard.charts.setChart.idle();
                           }, FloorDashboard.timeoutCount);
                        },
                        error: function (data) {
                            //console.log(data);
                           setTimeout(function () {
                             FloorDashboard.charts.setChart.idle();
                           }, FloorDashboard.timeoutCount);
                        },
                        dataType: "json"
                    });
                	
                }
            },
            getChart: {},
            chartConfig: {
                txRx: {
                    size: {
                        height: 220,
                    },
                    bindto: '#fd_chart2',

                    padding: {
                        top: 10,
                        right: 15,
                        bottom: 0,
                        left: 40,
                    },
                    data: {
                        columns: [],
                        types: {
                            Rx: 'area',
                            Tx: 'area-spline'
                        },
                        colors: {
                            Tx: '#5cd293',
                            Rx: '#1a78dd'
                        },
                        color: {
                            pattern: ['#2F9E63', '#1a78dd']
                        },
                        point: {
                            show: true
                        }
                    },
                     legend:{
                        item:{

                            "onclick":function(id){
                               FloorDashboard.charts.getChart.txRx.focus(id);  
                            }
                        }
                     },
                    tooltip: {
                        show: false
                    },
                    point: {
                        show: false
                    },
                    axis: {
                        x: {
                            type: 'category',
                            padding: {
                                left: -0.5,
                                right: -0.5,
                            },
                        },
                        y: {
                            padding: { bottom: 0 },
                            min: 0,
                            tick: {
                                format: d3.format("s")
                            }
                        },
                    }
                    
                },
                activeConnections: {
                    innerHTML: '',
                    showProgress: 1,
                    initialPos: 0,
                    targetPos: 3,
                    scale: 500,
                    rotateBy: 360 / 6,
                    speed: 900,
                    delayAnimation:false,
                    onFinishMoving: function (pos) {
                        //console.log('done ', pos);
                    }
                },
                netFlow: {
                    size: {
                        height: 220,
                    },
                    bindto: '#vdChart1',
                    padding: {
                        top: 10,
                        right: 15,
                        bottom: 0,
                        left: 55,
                    },
                    onresized: function () {
                        FloorDashboard.charts.getChart.netFlow.resize();
                    },
                    data: {

                    	x: 'radio',
                        columns: [
			                ['radio', 'Category1', 'Category2'],
			                ['ulink', 300, 400],
			                ['dlink', 300, 400]                   
                        ],

                        type:'bar',
                       
                        colors: {
                            ulink: '#f36e65',
                            dlink: '#1a78d0',
                        },
                    },
                    tooltip: {
                        show: true
                    },
                    point: {
                        show: false
                    },
                    axis: {
                    	rotated: true,
                        x: {
                            type: 'category'                     
                        },
                        y: {
                            padding: { bottom: 0 },
                            min: 0,
                            tick: {
                                format: d3.format("s")
                            }
                        },                        
                    }

                },
                typeOfDevices: {
                    size: {
                        height: 90,
                    },
                    bindto: '#dd-chart',
                    padding: {
                    	top: 15,
                        right: 2,
                        bottom:0,
                        left: 0,
                    },
                    transition: {
                	  duration: 500
                	},
                    data: {
                        columns: [],
                        names: {},
                        colors: {},
                        type: 'bar',
                    },
 
                	axis: {
                	    x: {
                	      show: false
                	    },
                	    y: {
                  	      show: false
                  	    }
                	},
                    tooltip: {
                        format: {
                        	title: function (v) {
                        		return "Active Tag Types";
                    		},
                    		value: function (i, j, k) {
                               	return 'Tags='+i;
                            }
                        }
                    }, 
                    legend: {
                        show: false
                    }
                },
                /*devicesConnected: {
                    size: {
                        height: 270,
                    },
                    bindto: '#fd_chart4',
                    padding: {
                        top: 0,
                        right: 15,
                        bottom: 0,
                        left: 15,
                    },
                    data: {
                        columns: [],
                        colors: {
                            "2G":  '#85d1fb',
                            "5G":  '#79d58a',
                            "BLE": '#79dfff',
                        },
                        type: 'donut'
                    },
                    donut: {
                        title: "",
                        label: {
                            threshold: 0.03,
                            format: function (value, ratio, id) {
                                0;
                            }
                        },
                        width: 40
                    },
                    tooltip: {
                        format: {
                            value: function (value, ratio, id) {
								if (value == 1) { return value + ' Tag';} else
                                	return value + ' Tags';
                            }
                        }
                    },
                    axis: {
                        x: {
                            show: false
                        }
                    },
                    legend: {
                        show: true
                    }

                },
                avgUplinkSpeed: {
                    innerHTML: '',
                    showProgress: 1,
                    initialPos: 0,
                    targetPos: 3,
                    scale: 100,
                    rotateBy: 360 / 6,
                    speed: 900,
                    delayAnimation:false,
                    onFinishMoving: function (pos) {
                       //console.log('done ', pos);
                    }

                },*/
                avgDownlinkSpeed: {

                    innerHTML: '',
                    showProgress: 1,
                    initialPos: 0,
                    targetPos: 3,
                    scale: 500,
                    rotateBy: 360 / 6,
                    speed: 900,
                    delayAnimation:false,
                    onFinishMoving: function (pos) {
                       //console.log('done ', pos);
                    }

                },
                idle: {

                    innerHTML: '',
                    showProgress: 1,
                    initialPos: 0,
                    targetPos: 3,
                    scale: 500,
                    rotateBy: 360 / 6,
                    speed: 900,
                    delayAnimation:false,
                    onFinishMoving: function (pos) {
                       //console.log('done ', pos);
                    }

                }
            }
        },
        init: function (params) {
            var c3ChartList = ['activeConnections', 'typeOfDevices','avgDownlinkSpeed','idle'];
            var that = this;
            $.each(c3ChartList, function (key, val) {
                that.charts.setChart[val](true,params?params:"");
            });
            this.systemAlerts();
        },
        systemAlerts:function(){
            $.ajax({
                url:'/facesix/rest/site/portion/networkdevice/alerts?spid='+urlObj.spid,
                method:'GET',
                success:function(result){
                     var result=result.length;
                     if(result==0){
                        $(".alert-gif").removeClass("hide").attr('src','/facesix/static/qubercomm/images/venue/correct.gif');
                        $(".alertText").text("All Systems Healthy");
                     }
                     else{
                        $(".alert-gif").removeClass("hide").attr('src','/facesix/static/qubercomm/images/venue/alert.gif');
                        $(".alertText").text("Alerts");
                     }       
                },
                error:function(){

                },
                dataType:'json'
            })
        }

    }
})();
currentDashboard=FloorDashboard;
var renderSummaryTemplate = function (data) {
    if (data && data.summary) {
        var source = $("#summary-template").html();
        var template = Handlebars.compile(source);
        var rendered = template(data);
        $('.summaryTable').html(rendered);
    }

}

var fetchSummaryTemplateData = function (){
    $.ajax({
        url: '/facesix/rest/site/portion/networkdevice/loginfo?spid='+urlObj.spid,
        success: function (result) {
        
        	//console.log (result);
            var templateObj={
                        data:[]
                     }

                     for(var i=0;i<result.length;i++){
                        var obj={};
                        obj.count=result[i].time;
                        obj.description=result[i].snapshot;
                        if(obj.description!=null)
                         templateObj.data.push(obj);
                     }
            renderSummaryTemplate({summary:templateObj.data});
        },
        error: function (data) {
            //console.log(data);
        },
        dataType: "json"
    })
}

var renderAlertsTemplate = function (data) {
    if (data && data.alerts) {
        var source = $("#alerts-template").html();
        var template = Handlebars.compile(source);
        var rendered = template(data);
        $('.summaryTable').html(rendered);
    }

}

var fetchAlertsTemplateData = function (){
    $.ajax({
        url: '/facesix/rest/site/portion/networkdevice/alerts?spid='+urlObj.spid,
        success: function (result) {

            var templateObj={
                data:[]
			}

            for(var i=0;i<result.length;i++){
            	var obj={};	
            	obj.description=result[i];
            	templateObj.data.push(obj);
            }
            		
            renderAlertsTemplate({alerts:templateObj.data});

        },
        error: function (data) {
            //console.log(data);
        },
        dataType: "json"
    })
}

$('body').on('click', '.viewAlertsTable', function (evt) {
    evt.preventDefault();
    window.clearInterval(fetchSummaryInterval);
    fetchAlertsTemplateData();
    window.fetchAlertsInterval = setInterval(function (){fetchAlertsTemplateData();}, FloorDashboard.timeoutCount);
});
$('body').on('click', '.viewSummaryTable', function () {
    window.clearInterval(fetchAlertsInterval);
    fetchSummaryTemplateData();
    window.fetchSummaryInterval = setInterval(function (){fetchSummaryTemplateData();}, FloorDashboard.timeoutCount);
});

//FloorDashboard.init();
//fetchSummaryTemplateData();
fetchAlertsTemplateData();
//window.fetchSummaryInterval = setInterval(function (){fetchSummaryTemplateData();}, FloorDashboard.timeoutCount);
window.fetchAlertsInterval  = setInterval(function (){fetchAlertsTemplateData();}, FloorDashboard.timeoutCount);
// fetchSummaryTemplateData();


//Network config Replica
var changetag = 1;
var circleval = 0;
var inactval  = 0;	

function showTag(v) {
	if (v == "1") {
		$('.person').show();
		$('.qrnd').show();
	} else {
		$('.person').hide();
		$('.qrnd').hide();
	}

}

function zoomicon(value) {
	if (value == "1") {
		$('.slider-section').show();
	} else {
		$('.slider-section').hide();
	}

}
var urlLink;
var tagtype  = "\uf007";
var color 	 = "#4337AE"//"#90EE90"
var tagcolor = "#FFA500";
var list;
var bDemofound = false;
var tagsCounter = 0;

var tagStatus 	= 0;

var filterTagactive = 1;
var filterCategories = [];
var zoomEnabled = 0;
var tagsONOFF = 1;
var inactiveONOFF = 1;
var switchONOFF = 0;
var category = [];
$('#tagsONOFF').change(function(){ 
	if($(this).prop('checked') == true){
		tagsONOFF = 1;
		d3.selectAll('.person').classed('tagdisable', false);
		$('.filterUI').addClass('active');
	}
	else{
		tagsONOFF = 0;
		d3.selectAll('.person').classed('tagdisable', true);
		$('.filterUI').removeClass('active');
	}  
});
$('#inactiveONOFF').change(function(){ 
	if($(this).prop('checked') == true){
		inactiveONOFF = 1; 
	}
	else{
		inactiveONOFF = 0; 
	} 
	if(tagsONOFF == 1){ 
		$.each(filterCategories, function(index, val) { 
			if(inactiveONOFF == 1){
				d3.selectAll('.person.'+val).classed('tagdisable', false);
			}
			else{
				d3.selectAll('.person.inactive').classed('tagdisable', true);
				d3.selectAll('.person.active.'+val).classed('tagdisable', false);
				d3.selectAll('.person.idle.'+val).classed('tagdisable', false);
			}
		});
	}
	else{
		d3.selectAll('.person').classed('tagdisable', true);
	}
});
$('#switchONOFF').change(function(){ 
	if($(this).prop('checked') == true){
		switchONOFF = 1; 
		d3.selectAll('.animatedImage').classed('tagdisable', false);	
	}
	else{
		switchONOFF = 0; 
		d3.selectAll('.animatedImage').classed('tagdisable', true);
	} 
});
$('.catFilter .multiselect-ui').change(function(){
	filterCategories = []; 
	$.each($(".catFilter .multiselect-ui option:selected"), function(){            
    	filterCategories.push($(this).val());  
    });  
	if(tagsONOFF == 1){ 
		$.each(filterCategories, function(index, val) { 
			if(inactiveONOFF == 1){
				d3.selectAll('.person.'+val).classed('tagdisable', false);
			}
			else{
				d3.selectAll('.person.inactive').classed('tagdisable', true);
				d3.selectAll('.person.active.'+val).classed('tagdisable', false);
				d3.selectAll('.person.idle.'+val).classed('tagdisable', false);
			}
		});
	}
	else{
		d3.selectAll('.person').classed('tagdisable', true);
	}
});


var floornetworkConfig={
		
   'plantDevicesTags' :function(urlSpid,p1, p2, p3){
	   
	   if (p1 != "true") {
		   //	$('.person').remove();
			// $('.qrnd').remove();
	      
	    	$.ajax({
	         	url:'/facesix/rest/site/portion/networkdevice/personinfo?spid='+urlSpid,
	             method:'get',
	             success:function(response){
	            	 list = response;
	             },  error:function(err){
	             }
	         });
        	
	    	
	    	/* var finalObj = [];
	    	function getRandomFloat(min, max) {
	    	  return Math.random() * (max - min) + min;
	    	}
	    	if (typeof list != "undefined" && list !="") {
		    	for (var i = 0; i < list.length; i++) {
		    		var count = checkxy(parseInt(list[i].x),parseInt(list[i].y),finalObj); 
		    		if(count > 0 && list[i].state == 'active'){ 
		    			list[i].x = parseInt(list[i].x) + getRandomFloat(-1,1);
		    			list[i].y = parseInt(list[i].x) + getRandomFloat(-1,1);  
		    			finalObj.push(list[i]);
		    		}else{
		    			finalObj.push(list[i]);
		    		}
		    	}
	    	}
	    	function checkxy(x,y, finalObj){ 
	    		var count = 0; 
	    		$.each(finalObj, function (key, val) {
	    			if((parseInt(val.x) == x) && parseInt(val.y) == y){
	    				count = count + 1;
	    			}
	    		});
	    		return count;
	    	}  
	    	*/
	    	function getRandomFloat(min, max) {
	    		return Math.random() * (max - min) + min;
	    	}
	    	if (typeof list != "undefined" && list !="") {
	    			
   	    		for (var i = 0; i < list.length; i++) {   
   	    			tags = list[i];
   	    			
   	    			category.push(tags.tagType); 
   	    			
   	    			var mcId = 'tags-'+tags.macaddr;
	    				mcId = mcId.replace(/:/g , "-");
   	    			var tagsFound = document.getElementById(mcId);
   	    			
   	    			if($( "g[data-x='"+tags.x+"'][data-y='"+tags.y+"']" ).length != 0){  
   	    				var newElement = $( "g[data-x='"+tags.x+"'][data-y='"+tags.y+"']" ).attr('id');
   	    				if(newElement != mcId){
   	    					tags.x = parseInt(tags.x) + getRandomFloat(-1,1);
	   	    				tags.y = parseInt(tags.y) + getRandomFloat(-1,1);
   	    				} 
   	    			}
   	    			
   	    				if (tags.state != undefined && tags.state != "") {        			
	              			state = tags.state       			
	              		}
	              		
        				if (state == "active"){        			
        					tagcolor  = "yellow";
        					strkcolor = "green"  
        					color 	  = "#3d3ef7" 
        					bgColor = "rgba(70, 191, 189, 0.5)";
        				} else if (state == "inactive"){        			
        					tagcolor  = "gray";  
        					strkcolor = "red";
        					color 	  = "#051a08" 
        					bgColor = "rgba(246, 70, 75, 0.5)";
        				} else if (state == "idle"){        			
  			        		tagcolor 	= "yellow";//"#FFA500";
        					strkcolor 	= "orange"; 
        					color 		= "#381a08"   
        					bgColor = "rgba(240, 114, 0, .5)";
        				}
	              	
	              		if (tags.tag_type != undefined && tags.tag_type != "") {
	              			tagtype = updateTagType(tags.tag_type);
	              		}
	              		
	              		var date = tags.lastReportingTime;;
	              		if(date == null || date == undefined){
	              			date = "Not Seen";
	              		}
	              		
	              if(tagsFound == null) {
	              			
	              		var mainGroup = this.svg.append('g')
	              			.style("cursor","pointer")
	              			.attr('id', mcId)
	              			.attr("fill", bgColor)
	              			.attr('data-x',tags.x)
	   	    				.attr('data-y',tags.y)
	              			.attr("class","person animateZoom "+state+" "+tags.tag_type)
	              			.attr("info","Name:"+tags.assignedTo + " <br/> Last Seen:" + date + "<br/> Location :" + tags.reciveralias)
	              			.attr('transform', "translate("+tags.x+","+tags.y+")") 
	              			.attr('data-html', 'true')
	              			.attr('title',"Name:"+tags.assignedTo + " <br/> Last Seen:" + date + "<br/> Location :" + tags.reciveralias);
	              			$(mainGroup).tooltip({container:'body'});
	
	              		var subGroup = mainGroup.append('g')
	              			.attr('id', mcId+'-sub') 
	              			.attr('transform','translate(0,0)') 
	              			.attr("class","onlyscale"); 
		
	              		var circle = subGroup.append("circle")
	              			.attr("r", fzie)
	              			.attr("y", "0").
	              			attr("class", "animateZoomCircle");
	              		var txt = subGroup.append("text") 
	              			.attr("alignment-baseline",'middle')  
	              			.attr("font-family","FontAwesome")
	              			.style("fill",'#fff')
	              			.style("cursor","pointer")  
	              			.attr("text-anchor", "middle") 
	              			.attr("y", txty) 
	              			.attr('font-size', function(d) { return fzie+'px';} )
	              			.text(function(d) { return tagtype; });
	  
      		        	tagsCounter = tagsCounter + 1;
      		        	
   	    			} else {
   	    				//console.log(state);
            			var macaddr = tags.macaddr;
               			
               			if (macaddr=="3F:23:AC:22:FF:F3" || macaddr=="3F:23:AC:22:FF:F4") { // test
               			
        	        		tags.x = tags.x*1+5+(Math.floor(Math.random() * 200));	
        	        		tags.y = tags.y*1+13+(Math.floor(Math.random() * 200));	
        	        		if (tags.x >= 1376) {
        	        			tags.x = 500;
        	        		}
        	        		if (tags.y >= 768) {
        	        			tags.y = 250;
        	        		}	
        	        		//console.log(" macaddr : " +macaddr);
        	        		bDemofound = true;
               			}
               			   			
               			this.svg.selectAll('#'+mcId).transition()
   	    			    .duration(1000)
   	    			    .attr("fill", bgColor)
   	    			    .attr('transform', "translate("+tags.x+","+tags.y+")");
               			this.svg.selectAll('#'+mcId).attr('data-x',tags.x);
   	    				this.svg.selectAll('#'+mcId).attr('data-y',tags.y);
   	    			}
   	    			 
  		        	
       	    	}
   	    		
  	   		}
  		
        }		
        

		var uniqueArray = function(arrArg) {
		  return arrArg.filter(function(elem, pos,arr) {
		    return arr.indexOf(elem) == pos;
		  });
		};

		var uniqEs6 = (arrArg) => {
		  return arrArg.filter((elem, pos, arr) => {
		    return arr.indexOf(elem) == pos;
		  });
		} 
	    	/* $.each(category, function(i, el){
	    	    if($.inArray(el, uniqueCat) === -1) uniqueCat.push(el);
	    	});  */
		var uniqueCat = [];
		function containsAny(source,target)
		{
		    var result = source.filter(function(item){ return target.indexOf(item) > -1});   
		    return (result.length > 0);  
		}  
		
		$('.catFilter .multiselect-ui option').each(function(){  
			uniqueCat.push(this.value)
		}); 
		//console.log();
	    	$.each(uniqueArray(category), function(index, optionValue) {  
	    		//$('.catFilter .multiselect-ui').append( $('<option selected></option>').val(optionValue).html(optionValue) );
	    		if(containsAny(category, uniqueCat) === false){
	    			$('.catFilter .multiselect-ui').append( $('<option selected></option>').val(optionValue).html(optionValue) );
	    		}
	    		// $('.catFilter .multiselect-ui').append( $('<option></option>').val(val).html(val) );

	    	});
	    	$('.catFilter .multiselect-ui').multiselect('rebuild');
	    	
	    	d3.selectAll('.person').classed('tagdisable', true);
	    	$.each($(".catFilter .multiselect-ui option:selected"), function(){            
	    	filterCategories.push($(this).val());  
	    	
	    });
	    	
	    	if(tagsONOFF == 1){ 
	    		$.each(filterCategories, function(index, val) { 
	    			if(inactiveONOFF == 1){
	    				d3.selectAll('.person.'+val).classed('tagdisable', false);
	    			}
	    			else{
	    				d3.selectAll('.person.inactive').classed('tagdisable', true);
	    				d3.selectAll('.person.active.'+val).classed('tagdisable', false);
	    				d3.selectAll('.person.idle.'+val).classed('tagdisable', false);
	    			}
	    	});
		}
		else{
			d3.selectAll('.person').classed('tagdisable', true);
		}
	    	
        if (bDemofound == true) {
        	bDemofound = false;
        	changetag = 10;
        }
        setTimeout(function() {
	     	floornetworkConfig.plantDevicesTags(urlSpid,p1, p2, p3);  
        }, changetag*1000);
  
	 },
	 
    'plantDevices':function(image,type,x,y,status,uid,cnt,tag, p1, p2, p3){
    
    	var obj;
    	var state = "active";
        var urlMap={
            "server":'dashboard',
            'switch':'swiboard',
            'ap':'devboard',
            'sensor':'devboard'
        }
        if (type == "server") {
            var url="/facesix/web/site/portion/"+urlMap[type]+"?sid="+this.urlObj.sid+"&spid="+this.urlObj.spid+"&cid="+this.urlObj.cid+"&type="+"server" 
        } else if (type == "sensor") {
            var url="/facesix/web/finder/device/"+urlMap[type]+"?sid="+this.urlObj.sid+"&spid="+this.urlObj.spid+"&uid="+uid+"&cid="+this.urlObj.cid+"&type="+"sensor" 
        } else if (p1 == true){
            var url="/facesix/web/site/portion/"+urlMap[type]+"?sid="+this.urlObj.sid+"&uid="+uid+"&cid="+this.urlObj.cid+"&type="+(type=="switch" || type=="server"?type:"device")+"&spid="+this.urlObj.spid 
        }   
        
        var anchor=this.svg.append("a").attr("xlink:href",url);
        var newImage=anchor.append("image")
        .attr({
            'x':x,
            'y':y,
            'xlink:href':image,
            'status':status,
            'height':imageH,
            'width':imageW,
            'class':'animatedImage',
            'data-uid':uid,
            'type':type
        });
       
     },
     showPopupMenu:function(evt){
            evt.preventDefault();
            var offsets=$(this).offset();
            var status=$(evt.target).attr("status");
            var uid=$(evt.target).attr("data-uid");
            $(".viewActivity").attr("href",$(this).attr("href"))
            $(".powerBtn").attr("uid",uid);
            floornetworkConfig.moveElementandShow(offsets,uid,status)
     },
     moveElementandShow:function(offsets,uid,status){
            $("#deviceHeading").text(uid);
            $("#status").text(status);
            $(".networkconfig").show().css({
                'position':'absolute',
                'left':offsets.left+(imageW/2),
                'top':offsets.top+(imageH/2)
            });
     },
     'fetchurlParams':function(search){
        var urlObj={}
        if(search)
          urlObj=JSON.parse('{"' + decodeURI(search).replace(/"/g, '\\"').replace(/&/g, '","').replace(/=/g,'":"') + '"}')
        this.urlObj=urlObj;
        return urlObj; 
    },
    getDevices:function(param1, param2,param3){
    	var that=this;
        var urlObj=this.fetchurlParams(window.location.search.substr(1));
        var urlObjSpid = urlObj.spid;
        var urlLink ='/facesix/rest/site/portion/networkdevice/list?spid='+urlObjSpid;
    	var taginfo;
     	var person=0;

     	//console.log (" param1  " + param1 +" param2 "+ param2 +" param3 " +param3)
            
        $.ajax({
            url:urlLink,
            method:'get',
            success:function(response){
            	 
            	var devices=response;
            	 
            	var ii=0;
                for(ii=0;ii<devices.length;ii++)
                {
                	if(devices[ii].parent=="ble")
                		var type="sensor";
                	else
                		var type=devices[ii].typefs;
                		
                    var status  = devices[ii].status;
                    var image	= "/facesix/static/qubercomm/images/networkicons/"+type+"_"+status+".png";
                    var uid		= devices[ii].uid;
                    
                    if (param1 == "true") {
                        taginfo 	= devices[ii].tagstring;
                        person 		= devices[ii].activetag;
                    }
                    
                    that.plantDevices(image,type,devices[ii].xposition,devices[ii].yposition,status,uid,person, taginfo, param1, param2,param3)
                }	
            },
            error:function(err){
                console.log(	err);    
            }
        });
        
     	 if (param1 != "true") {
         	that.plantDevicesTags(urlObjSpid,param1, param2,param3);
         }
     	 
     }
}
$("#closebutton").on('click',function(evt){
    evt.preventDefault();
    $(".networkconfig").hide();
})
$(document).on('click',function(evt){
    $(".networkconfig").hide();
})  


//fullscreen network map
    $('.enlarge').click(function(e){
        $('.floorCan').toggleClass('deviceexpand');
        $('.floorCan').toggleClass('pad0');
        $('.na-panel').toggleClass('height100');
    });
/*
$(".panzoom").panzoom({
	$zoomIn: $(".zoom-in"),
	$zoomOut: $(".zoom-out"),
	$zoomRange:$(".zoom-range"),
	$reset: $(".reset"),
	contain:'automatic',
	increment:1,
	minScale:1,
	maxScale:5
});
*/
$('#floorName').on('change', function() {
	
	var spid    =$('#floorName').val();
	var sid  	= location.search.split("&")[0].replace("?","").split("=")[1];
	var cid 	= location.search.split("&")[2].replace("?","").split("=")[1];
    
	var url  = "/facesix/web/site/portion/dashboard?sid="+sid+"&spid="+spid+"&cid="+cid;
	
	//console.log("url" + url);
	
	$.ajax({
  	   	  	url:url,
  	   	  	method:'GET',
  	   	  	data:{},
  	   	  	success:function(response,error){
  	   	  	location.replace(url);
  	   	  	},
  	   	  	error:function(error){
  	   	  		 console.log(error);
  	   	  	}
  	   	  });
	
});


$('#floorfresh').on('change',function(){
	 changetag=document.getElementById('floorfresh').value;
	 //console.log("floorfreshtag" + changetag);
});

$('#circle').on('change',function(){
	 circleval = document.getElementById('circle').value;
	 //console.log("circle" + circleval);
});
$('#inactive').on('change',function(){
	 inactval = document.getElementById('inactive').value;
	 //console.log("inactive" + inactval);
});

function updateTagType(tag_type) {
	
	
	var  code = "\uf007"; //default tag Type

	if (tag_type != null) {

		if (tag_type=="Doctor") {
			code = "\uf0f0";
		} else if (tag_type=="WheelChair") {
			code = "\uf193";
		} else if (tag_type=="Asset") {
			code = "\uf217";
		} else if (tag_type=="Bed") {
			code = "\uf236";
		} else if (tag_type=="Ambulance") {
			code = "\uf0f9";
		} else if (tag_type=="MedicalKit") {
			code = "\uf0fa";
		} else if (tag_type=="Heartbeat") {
			code = "\uf21e";
		} else if (tag_type=="Cycle") {
			code = "\uf206";
		} else if (tag_type=="Truck") {
			code = "\uf0d1";
		} else if (tag_type=="Bus") {
			code = "\uf207";
		} else if (tag_type=="Car") {
			code = "\uf1b9";
		} else if (tag_type=="Child") {
			code = "\uf1ae";
		} else if (tag_type=="Female") {
			code = "\uf182";
		} else if (tag_type=="Male") {
			code = "\uf183";
		} else if (tag_type=="Fax") {
			code = "\uf1ac";
		} else if (tag_type=="User") {
			code = "\uf007";
		} else if (tag_type=="Library") {
			code = "\uf02d";
		} else if (tag_type=="Hotel") {
			code = "\uf0f5";
		} else if (tag_type=="Fireextinguisher") {
			code = "\uf134";
		} else if (tag_type=="Print") {
			code = "\uf02f";
		} else if (tag_type=="Clock") {
			code = "\uf017";
		} else if (tag_type=="Film") {
			code = "\uf008";
		} else if (tag_type=="Music") {
			code = "\uf001";
		} else if (tag_type=="Levelup") {
			code = "\uf148";
		} else if (tag_type=="Leveldown") {
			code = "\uf149";
		} else if (tag_type=="Trash") {
			code = "\uf014";
		} else if (tag_type=="Home") {
			code = "\uf015";
		} else if (tag_type=="Videocamera") {
			code = "\uf03d";
		} else if (tag_type=="Circle") {
			code = "\uf05a";
		} else if (tag_type=="Gift") {
			code = "\uf06b";
		} else if (tag_type=="Exit") {
			code = "\uf08b";
		} else if (tag_type=="Key") {
			code = "\uf084";
		} else if (tag_type=="Camera") {
			code = "\uf083";
		} else if (tag_type=="Phone") {
			code = "\uf083";
		} else if (tag_type=="Creditcard") {
			code = "\uf09d";
		} else if (tag_type=="Speaker") {
			code = "\uf0a1"; 
		} else if (tag_type=="Powerroom") {
			code = "\uf1e6";
		} else if (tag_type=="Toolset") {
			code = "\uf0ad";
		} else if (tag_type=="Batteryroom") {
			code = "\uf241";
		} else if (tag_type=="Computerroom") {
			code = "\uf241";
		} else if (tag_type=="Kidsroom") {
			code = "\uf113";
		} else if (tag_type=="TVroom") {
			code = "\uf26c";
		} else {
			code = "\uf007";
		}
	}

return code;
}

