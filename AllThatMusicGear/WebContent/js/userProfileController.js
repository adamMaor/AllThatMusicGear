/**
 * 
 */

angular.module('mainPageApp').controller('userProfile', ['$scope', '$http', '$location', function($scope, $http, $location) {
	
	$scope.userNickname = $location.hash();
	
	var parameters = {
			params: {
				userNickName: $scope.userNickname,
			}};
	
	$http.get("UserServlet/GetUserInfo", parameters)
	.success(function(response) {
		$scope.user = angular.copy(response);
	});
	$http.get("QandAServlet/UserLastAskedQuestions", parameters)
	.success(function(response) {
		$scope.lastAsked = angular.copy(response);
	});
	$http.get("QandAServlet/UserLastAnswerdAnswers", parameters)
	.success(function(response) {
		$scope.lastAnswered = angular.copy(response);
	});
	
}]);