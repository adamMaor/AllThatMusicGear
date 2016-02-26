/**
 * leaderboardCtrl - Controller that handles leaderboards page,
 * getting user information from server depending on current page
 */

angular.module('mainPageApp').controller('leaderboardCtrl', ['$scope', '$http', function($scope, $http) {
	$scope.pageNum = 1;
	$scope.maxPageNum = 1;
	var parameter = { params: { pageNum: $scope.pageNum,} };
	
	// get 20 users according to current page sent in parameter
	$scope.updateLeaderboard = function(){
		$http.get("UserServlet/GetAllUsersInfo", parameter)
		.success(function(response){
			$scope.allUsers = response.users;
			var totalUsers = parseInt(response.numUsers);
			$scope.maxPageNum = parseInt((totalUsers-1)/20) + 1;
			$scope.userCount = totalUsers;
		});
	};
	
	$scope.updateLeaderboard();
	
	$scope.nextPage = function(){
		if ($scope.pageNum < $scope.maxPageNum){
			$scope.pageNum += 1;
			parameter = { params: { pageNum: $scope.pageNum,} };
			$scope.updateLeaderboard();
		}
	}
	
	$scope.prevPage = function(){
		if ($scope.pageNum > 1){
			$scope.pageNum -= 1;
			parameter = { params: { pageNum: $scope.pageNum,} };
			$scope.updateLeaderboard();
		}	
	}
}]);