<script lang="ts">
	import { Table, TableBody, TableBodyCell, TableBodyRow } from 'flowbite-svelte';
	import type { TimeStampValidationResult } from '../libs/openapi';

	export let validationResult: TimeStampValidationResult;
</script>

<Table>
	<caption
		class="p-5 text-lg font-semibold text-left text-gray-900 bg-white dark:text-white dark:bg-gray-800"
	>
		Result
	</caption>
	<TableBody class="divide-y">
		<TableBodyRow>
			<TableBodyCell>Status</TableBodyCell>
			<TableBodyCell>
				{validationResult.status}{#if validationResult.statusString}({validationResult.statusString}){/if}
			</TableBodyCell>
		</TableBodyRow>
		{#if validationResult.failureInfo}
			<TableBodyRow>
				<TableBodyCell>Failure Info</TableBodyCell>
				<TableBodyCell>{validationResult.failureInfo}</TableBodyCell>
			</TableBodyRow>
		{:else}
			<TableBodyRow>
				<TableBodyCell>Generation Time</TableBodyCell>
				<TableBodyCell>{validationResult.generationTime}</TableBodyCell>
			</TableBodyRow>
			<TableBodyRow>
				<TableBodyCell>Serial Number (Hex)</TableBodyCell>
				<TableBodyCell>{validationResult.serialNumber.toString(16)}</TableBodyCell>
			</TableBodyRow>
			<TableBodyRow>
				<TableBodyCell>Hash Algorithm OID</TableBodyCell>
				<TableBodyCell>
					<a href="http://oid-info.com/get/{validationResult.hashAlgorithmIdentifier}">
						{validationResult.hashAlgorithmIdentifier}
					</a>
				</TableBodyCell>
			</TableBodyRow>
			<TableBodyRow>
				<TableBodyCell>Hash</TableBodyCell>
				<TableBodyCell>{validationResult.hash}</TableBodyCell>
			</TableBodyRow>
			<TableBodyRow>
				<TableBodyCell>Signature valid</TableBodyCell>
				<TableBodyCell>{validationResult.signatureValid ? 'Yes' : 'No'}</TableBodyCell>
			</TableBodyRow>
		{/if}
	</TableBody>
</Table>
