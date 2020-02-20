cmake_minimum_required(VERSION 3.5)
set(CMAKE_CXX_STANDARD 11)
project(decisiontreeclassifier LANGUAGES CXX)

find_package(aws-lambda-runtime REQUIRED)
add_executable(${PROJECT_NAME} ${PROJECT_SOURCE_DIR}/driver.cpp ${PROJECT_SOURCE_DIR}/node.cpp ${PROJECT_SOURCE_DIR}/ID3Algorithm.cpp ${PROJECT_SOURCE_DIR}/helper.cpp ${PROJECT_SOURCE_DIR}/FileProcessor.cpp)
target_link_libraries(${PROJECT_NAME} PUBLIC AWS::aws-lambda-runtime)
aws_lambda_package_target(${PROJECT_NAME})
