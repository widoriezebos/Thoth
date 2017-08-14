# Start Consul
We will use Consul as a shared registry where we can store our configuration property file. The following command will start Consul with the UI enabled (/ui on on port 8500)
	docker run -d -v consul-data:/consul/data -p 8500:8500 consul agent -node-id=$(uuidgen | awk '{print tolower($0)}') -bootstrap -server -client=0.0.0.0 -ui
	 

Note that we store the state of the Consul (single) node in the Docker volume named `consul-data` so that we keep our state across restarts. Note that the command below is not meant for production purposes; this will just get you a single node cluster that stores state.

