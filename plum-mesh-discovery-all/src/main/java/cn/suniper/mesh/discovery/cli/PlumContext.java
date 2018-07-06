package cn.suniper.mesh.discovery.cli;

import cn.suniper.mesh.discovery.KVStore;
import cn.suniper.mesh.discovery.ProviderDelegatingRegister;
import cn.suniper.mesh.discovery.RegisteredServerDynamicList;
import cn.suniper.mesh.discovery.annotation.ClientTypeEnum;
import cn.suniper.mesh.discovery.commons.ConfigManager;
import cn.suniper.mesh.discovery.commons.ConnAutoInitializer;
import cn.suniper.mesh.discovery.commons.KvSource;
import cn.suniper.mesh.discovery.model.Application;
import com.netflix.client.IClient;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Rao Mengnan
 *         on 2018/7/5.
 */
public class PlumContext {
    // 是否为服务提供者
    private boolean asServiceProvider;
    // 注册中心服务提供者类型
    private KvSource.Provider providerType;
    // 初始化的KvStore
    private KVStore kvStore;
    // KvStore的客户端连接
    private Object kvStoreSource;
    // 客户端类型与实例映射
    private Map<ClientTypeEnum, IClient> clientMap;

    private ProviderDelegatingRegister register;
    private RegisteredServerDynamicList dynamicList;
    private ConfigManager configManager;

    public PlumContext(boolean asServiceProvider, KvSource.Provider providerType) {
        this.asServiceProvider = asServiceProvider;
        this.providerType = providerType;

        this.clientMap = new HashMap<>();
    }

    public void initPlumApp() throws InterruptedException, IOException, ClassNotFoundException {
        Application application = configManager.getApplication();
        if (application == null) {
            throw new IllegalStateException("Application information initialize failed");
        }

        switch (providerType) {
            case AUTO:
                if (kvStoreSource == null) {
                    kvStore = ConnAutoInitializer.getStore(application);
                } else {
                    kvStore = ConnAutoInitializer.getStore(kvStoreSource);
                }
                break;
            default:
                if (kvStoreSource == null) {
                    kvStore = ConnAutoInitializer.getStore(providerType, application);
                } else {
                    kvStore = ConnAutoInitializer.getStore(providerType, kvStoreSource);
                }
        }

        if (asServiceProvider) {
            register = new ProviderDelegatingRegister(kvStore, application);
        } else {
            dynamicList = new RegisteredServerDynamicList(kvStore, application.getName());
        }
    }

    public boolean isAsServiceProvider() {
        return asServiceProvider;
    }

    public KVStore getKvStore() {
        return kvStore;
    }

    public PlumContext setKvStoreSource(Object kvStoreSource) {
        this.kvStoreSource = kvStoreSource;
        return this;
    }

    public ProviderDelegatingRegister getRegisterAgent() {
        return register;
    }

    public RegisteredServerDynamicList getDynamicServerList() {
        return dynamicList;
    }

    public PlumContext setConfigManager(ConfigManager configManager) {
        this.configManager = configManager;
        return this;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public void putClient(ClientTypeEnum clientType, IClient client) {
        clientMap.put(clientType, client);
    }
}
