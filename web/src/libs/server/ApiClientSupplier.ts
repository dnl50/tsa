import { Configuration, TsaResourceApi, type TsaResourceApiInterface } from '../openapi';
import { env } from '$env/dynamic/private';

const apiClientConfiguration: Configuration = new Configuration({
	basePath: env.API_BASE_PATH
});

export const tsaApi: TsaResourceApiInterface = new TsaResourceApi(apiClientConfiguration);
