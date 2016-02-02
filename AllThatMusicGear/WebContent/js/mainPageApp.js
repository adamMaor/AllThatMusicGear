var mainPageApp = angular.module('mainPageApp',[]);
mainPageApp.controller('mainPageAppController', ['$scope', '$http', function($scope, $http) {
	 $scope.http = $http;
	 $scope.loggedInUserNickName = "Adam";
 }]);