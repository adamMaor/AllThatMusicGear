/**
 * Define main module and common functions 
 */

var mainPageApp = angular.module('mainPageApp',[]);

mainPageApp.controller('mainController', ['$scope', function($scope){
	$scope.templates = 
		[ { name: 'header', url: '/AllThatMusicGear/html-resources/header.html'},
		  { name: 'questionThread', url: '/AllThatMusicGear/html-resources/questionsthread.html'} ];
	$scope.header = $scope.templates[0];
	$scope.questionThread = $scope.templates[1];
}]);

var loggedInUser = "null";
var checkLogin = function () {
	$.get("UserServlet/GetSessionInfo", function(data, status){
		if (data.nickName == "null"){		
			window.location.href = '/AllThatMusicGear/login.html';		
		}
		else{
			loggedInUser = jQuery.parseJSON(data).nickName;
		}
	});
};

// we are using hash values in url's, by default
// hash change does not trigger refresh, a behaviour we'd like
// so we force refresh on hash change
window.onhashchange = function() {
	window.location.reload();
};

