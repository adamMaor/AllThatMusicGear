/**
 * 
 */

angular.module('mainPageApp').controller('topicsCtrl', ['$scope', '$http', function($scope, $http) {
	$scope.pageNum = 1;
	$scope.maxPageNum = 1;
	$scope.tagsOnPage = 50;
	var parameter = { params: { offset: ($scope.pageNum-1)*$scope.tagsOnPage, listSize: $scope.tagsOnPage} };
	
	$scope.updateTopics = function(){
		$http.get("QandAServlet/QuestionTopicsByTpop", parameter)
		.success(function(response){
			$scope.topics = response.topics;
			$scope.topicCount = response.numTopics;
			$scope.maxPageNum = parseInt((response.numTopics-1)/$scope.tagsOnPage) + 1;
			$scope.shuffleArray($scope.topics);
			// calculate average popularity score for the tag cloud
			$scope.tPopAvg = 0;
			for (var i = 0; i<$scope.topics.length; i++){
				$scope.tPopAvg += $scope.topics[i].tPop;
			}
			$scope.tPopAvg /= $scope.topics.length;
			if ($scope.tPopAvg < 0){
				$scope.tPopAvg = -$scope.tPopAvg;
			}
			if($scope.tPopAvg == 0){
				$scope.tPopAvg = 1;
			}
		});
	};
	
	$scope.updateTopics();
	
	$scope.nextPage = function(){
		if ($scope.pageNum < $scope.maxPageNum){
			$scope.pageNum += 1;
			parameter = { params: { offset: ($scope.pageNum-1)*$scope.tagsOnPage, listSize: $scope.tagsOnPage} };
			$scope.updateTopics();
		}
	}
	
	$scope.prevPage = function(){
		if ($scope.pageNum > 1){
			$scope.pageNum -= 1;
			parameter = { params: { offset: ($scope.pageNum-1)*$scope.tagsOnPage, listSize: $scope.tagsOnPage} };
			$scope.updateTopics();
		}	
	}
	
	//Randomize array element order in-place.
	$scope.shuffleArray = function (array) {
	    for (var i = array.length - 1; i > 0; i--) {
	        var j = Math.floor(Math.random() * (i + 1));
	        var temp = array[i];
	        array[i] = array[j];
	        array[j] = temp;
	    }
	    return array;
	}
	
	$scope.topicCloudStyle = function(topicPop){
		var minFontSize = 100;
		var maxFontSize = 400;
		var topicPopAboveAvgPer = (topicPop / $scope.tPopAvg) * 100;
		if (topicPopAboveAvgPer > maxFontSize){
			topicPopAboveAvgPer = maxFontSize;
		}
		if (topicPopAboveAvgPer < minFontSize){
			topicPopAboveAvgPer = minFontSize;
		}
		
		return {"font-size" : topicPopAboveAvgPer+"%",
		};
	};
}]);
